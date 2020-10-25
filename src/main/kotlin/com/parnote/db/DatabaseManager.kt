package com.parnote.db

import com.parnote.util.ConfigManager
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.logging.Logger
import io.vertx.ext.asyncsql.AsyncSQLClient
import io.vertx.ext.asyncsql.MySQLClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.kotlin.core.json.jsonObjectOf

class DatabaseManager(
    private val mVertx: Vertx,
    private val mLogger: Logger,
    private val mConfigManager: ConfigManager
) {
    private lateinit var mAsyncSQLClient: AsyncSQLClient

    private val mDatabase by lazy {
        Database()
    }

    private val mMigrations = listOf<DatabaseMigration>(
    )

    companion object {
        const val DATABASE_SCHEME_VERSION = 1
        const val DATABASE_SCHEME_VERSION_INFO = "Initial database"
    }

    init {
        checkMigration()
    }

    private fun checkMigration() {
        createConnection { connection, _ ->
            if (connection != null) {
                mDatabase.schemeVersionDao.getLastSchemeVersion(getSQLConnection(connection)) { schemeVersion ->
                    if (schemeVersion == null)
                        initDatabase(connection)
                    else {
                        val databaseVersion = schemeVersion.key.toIntOrNull() ?: 0

                        if (databaseVersion == 0)
                            initDatabase(connection)
                        else
                            migrate(connection, getSQLConnection(connection), databaseVersion)
                    }
                }
            }
        }
    }

    private fun migrate(connection: Connection, sqlConnection: SQLConnection, databaseVersion: Int) {
        val handlers = mMigrations.map { it.migrate(sqlConnection, getTablePrefix()) }

        var currentIndex = 0

        fun invoke() {
            val localHandler: (AsyncResult<*>) -> Unit = {
                fun check() {
                    when {
                        it.failed() -> closeConnection(connection) {
                            mLogger.error("Database Error: Migration failed from version ${mMigrations[currentIndex].FROM_SCHEME_VERSION} to ${mMigrations[currentIndex].SCHEME_VERSION}")
                        }
                        currentIndex == handlers.lastIndex -> closeConnection(connection)
                        else -> {
                            currentIndex++

                            invoke()
                        }
                    }
                }

                if (it.succeeded())
                    mMigrations[currentIndex].updateSchemeVersion(sqlConnection)
                        .invoke { updateSchemeVersion ->
                            if (updateSchemeVersion.failed())
                                closeConnection(connection) {
                                    mLogger.error("Database Error: Migration failed from version ${mMigrations[currentIndex].FROM_SCHEME_VERSION} to ${mMigrations[currentIndex].SCHEME_VERSION}")
                                }
                            else
                                check()
                        }
                else
                    check()
            }

            if (mMigrations[currentIndex].isMigratable(databaseVersion)) {
                if (currentIndex <= handlers.lastIndex)
                    handlers[currentIndex].invoke(localHandler)
            } else if (currentIndex == handlers.lastIndex)
                closeConnection(connection)
            else {
                currentIndex++

                invoke()
            }
        }

        invoke()
    }

    fun createConnection(handler: (connection: Connection?, asyncResult: AsyncResult<SQLConnection>) -> Unit) {
        if (!::mAsyncSQLClient.isInitialized) {
            val databaseConfig = (mConfigManager.getConfig()["database"] as Map<*, *>)

            var port = 3306
            var host = databaseConfig["host"] as String

            if (host.contains(":")) {
                val splitHost = host.split(":")

                host = splitHost[0]

                port = splitHost[1].toInt()
            }

            val mySQLClientConfig = jsonObjectOf(
                Pair("host", host),
                Pair("port", port),
                Pair("database", databaseConfig["name"]),
                Pair("username", databaseConfig["username"]),
                Pair("password", if (databaseConfig["password"] == "") null else databaseConfig["password"])
            )

            mAsyncSQLClient = MySQLClient.createShared(mVertx, mySQLClientConfig, "MysqlLoginPool")
        }

        Connection.createConnection(mLogger, mAsyncSQLClient) { connection, asyncResult ->
            handler.invoke(connection, asyncResult)
        }
    }

    fun closeConnection(connection: Connection, handler: ((asyncResult: AsyncResult<Void?>?) -> Unit)? = null) {
        connection.closeConnection(handler)
    }

    fun getSQLConnection(connection: Connection) = connection.getSQLConnection()

    private fun initDatabase(connection: Connection, handler: (asyncResult: AsyncResult<*>) -> Unit = {}) {
        val databaseInitProcessHandlers = mDatabase.init(getSQLConnection(connection))

        var currentIndex = 0

        fun invoke() {
            val localHandler: (AsyncResult<*>) -> Unit = {
                when {
                    it.failed() || currentIndex == databaseInitProcessHandlers.lastIndex -> closeConnection(
                        connection
                    ) { _ ->
                        handler.invoke(it)
                    }
                    else -> {
                        currentIndex++

                        invoke()
                    }
                }
            }

            if (currentIndex <= databaseInitProcessHandlers.lastIndex)
                databaseInitProcessHandlers[currentIndex].invoke(localHandler)
        }

        invoke()
    }

    fun getDatabase() = mDatabase

    fun getTablePrefix() = (mConfigManager.getConfig()["database"] as Map<*, *>)["prefix"].toString()
}