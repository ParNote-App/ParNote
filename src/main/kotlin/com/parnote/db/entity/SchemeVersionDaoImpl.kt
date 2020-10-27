package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.DatabaseManager.Companion.DATABASE_SCHEME_VERSION
import com.parnote.db.DatabaseManager.Companion.DATABASE_SCHEME_VERSION_INFO
import com.parnote.db.dao.SchemeVersionDao
import com.parnote.db.model.SchemeVersion
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class SchemeVersionDaoImpl(override val tableName: String = "scheme_version") : DaoImpl(), SchemeVersionDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
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
                getLastSchemeVersion(sqlConnection) { schemeVersion, asyncResult ->
                    if (schemeVersion == null)
                        add(
                            sqlConnection,
                            SchemeVersion(DATABASE_SCHEME_VERSION.toString(), DATABASE_SCHEME_VERSION_INFO)
                        ) { _, asyncResultAdd ->
                            handler.invoke(asyncResultAdd)
                        }
                    else {
                        val databaseVersion = schemeVersion.key.toIntOrNull() ?: 0

                        if (databaseVersion == 0)
                            add(
                                sqlConnection,
                                SchemeVersion(DATABASE_SCHEME_VERSION.toString(), DATABASE_SCHEME_VERSION_INFO)
                            ) { _, asyncResultAdd ->
                                handler.invoke(asyncResultAdd)
                            }
                        else
                            handler.invoke(asyncResult)
                    }

                }
            else
                handler.invoke(it)
        }
    }

    override fun add(
        sqlConnection: SQLConnection,
        schemeVersion: SchemeVersion,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) = sqlConnection.updateWithParams(
        """
            INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (`key`, `extra`) VALUES (?, ?)
        """.trimIndent(),
        JsonArray()
            .add(schemeVersion.key)
            .add(schemeVersion.extra)
    ) {
        handler.invoke(if (it.succeeded()) Successful() else null, it)
    }!!

    override fun getLastSchemeVersion(
        sqlConnection: SQLConnection,
        handler: (schemeVersion: SchemeVersion?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query = "SELECT MAX(`key`) FROM `${databaseManager.getTablePrefix() + tableName}`"

        sqlConnection.query(
            query
        ) { queryResult ->
            if (queryResult.failed()) {
                handler.invoke(null, queryResult)

                return@query
            }

            handler.invoke(
                if (queryResult.result().results[0].getString(0) == null) null else SchemeVersion(
                    queryResult.result().results[0].getString(
                        0
                    ), null
                ), queryResult
            )
        }
    }
}