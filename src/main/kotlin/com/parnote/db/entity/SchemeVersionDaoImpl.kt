package com.parnote.db.entity

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DaoImpl
import com.parnote.db.DatabaseManager
import com.parnote.db.DatabaseManager.Companion.DATABASE_SCHEME_VERSION
import com.parnote.db.DatabaseManager.Companion.DATABASE_SCHEME_VERSION_INFO
import com.parnote.db.dao.SchemeVersionDao
import com.parnote.model.Error
import com.parnote.model.Result
import com.parnote.model.SchemeVersion
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import javax.inject.Inject

class SchemeVersionDaoImpl(override val tableName: String = "scheme_version") : DaoImpl(), SchemeVersionDao {

    @Inject
    lateinit var databaseManager: DatabaseManager

    init {
        Main.getComponent().inject(this)
    }

    override fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        sqlConnection.query(
            """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `when` timestamp not null default CURRENT_TIMESTAMP,
              `key` varchar(255) not null,
              `extra` varchar(255),
              PRIMARY KEY (`key`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Database scheme version table.';
        """
        ) {
            if (it.succeeded())
                sqlConnection.queryWithParams(
                    """
                        SELECT COUNT(`key`) FROM `${databaseManager.getTablePrefix() + tableName}` where `key` = ?
            """.trimIndent(),
                    JsonArray().add(DATABASE_SCHEME_VERSION.toString())
                ) {
                    if (it.failed() || it.result().results[0].getInteger(0) != 0)
                        handler.invoke(it)
                    else
                        sqlConnection.updateWithParams(
                            """
                        INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (`key`, `extra`) VALUES (?, ?)
            """.trimIndent(),
                            JsonArray()
                                .add(DATABASE_SCHEME_VERSION.toString())
                                .add(DATABASE_SCHEME_VERSION_INFO)
                        ) {
                            handler.invoke(it)
                        }
                }
            else
                handler.invoke(it)
        }
    }

    override fun add(
        sqlConnection: SQLConnection,
        schemeVersion: SchemeVersion,
        handler: (result: Result, asyncResult: AsyncResult<*>) -> Unit
    ) = sqlConnection.updateWithParams(
        """
                            INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (`key`, `extra`) VALUES (?, ?)
                """.trimIndent(),
        JsonArray()
            .add(schemeVersion.key)
            .add(schemeVersion.extra)
    ) {
        handler.invoke(if (it.succeeded()) Successful() else Error(ErrorCode.SCHEME_VERSION_ADD_ERROR), it)
    }!!

    override fun add(schemeVersion: SchemeVersion, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { connection, _ ->
            if (connection != null) {
                add(databaseManager.getSQLConnection(connection), schemeVersion) { result, _ ->
                    databaseManager.closeConnection(connection) {
                        handler.invoke(result)
                    }
                }
            }
        }
    }

    override fun getLastSchemeVersion(
        sqlConnection: SQLConnection,
        handler: (schemeVersion: SchemeVersion?) -> Unit
    ) {
        val query = "SELECT MAX(`key`) FROM `${databaseManager.getTablePrefix() + tableName}`"

        sqlConnection.query(
            query
        ) { queryResult ->
            if (queryResult.failed()) {
                handler.invoke(null)

                return@query
            }

            handler.invoke(SchemeVersion(queryResult.result().results[0].getString(0), null))
        }
    }
}