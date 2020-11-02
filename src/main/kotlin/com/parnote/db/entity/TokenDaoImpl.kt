package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.TokenDao
import com.parnote.db.model.Token
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class TokenDaoImpl(override val tableName: String = "token") : DaoImpl(), TokenDao {
    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `token` text NOT NULL,
              `created_time` MEDIUMTEXT NOT NULL,
              `user_id` int(11) NOT NULL,
              `subject` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Token Table';
        """
            ) {
                handler.invoke(it)
            }
        }

    override fun add(
        token: Token,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.updateWithParams(
            """
                INSERT INTO `${databaseManager.getTablePrefix() + tableName}` (token, created_time, user_id, subject) VALUES (?, ?, ?, ?)
            """.trimIndent(),
            JsonArray()
                .add(token.token)
                .add(System.currentTimeMillis())
                .add(token.userID)
                .add(token.subject)
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }

    override fun getUserIDFromToken(
        token: String,
        sqlConnection: SQLConnection,
        handler: (result: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT user_id FROM `${databaseManager.getTablePrefix() + tableName}` where `token` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun delete(
        token: Token,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${databaseManager.getTablePrefix() + tableName}` WHERE token = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(token.token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isTokenExists(
        token: String,
        sqlConnection: SQLConnection,
        handler: (exits: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${databaseManager.getTablePrefix() + tableName}` where `token` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getCreatedTimeByToken(
        token: String,
        sqlConnection: SQLConnection,
        handler: (createdTime: Long?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT created_time FROM `${databaseManager.getTablePrefix() + tableName}` where `token` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(token)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getString(0).toLong(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}