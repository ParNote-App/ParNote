package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.ShareLinkDao
import com.parnote.db.model.ShareLink
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

class ShareLinkDaoImpl(override val tableName: String = "share_link") : DaoImpl(), ShareLinkDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `note_id` int(11) NOT NULL UNIQUE,
              `token_id` int NOT NULL UNIQUE,
                constraint ${getTablePrefix()}share_link_${getTablePrefix()}note_id_fk
                    foreign key (note_id) references ${getTablePrefix()}note (id),
                constraint ${getTablePrefix()}share_link_${getTablePrefix()}token_id_fk
                    foreign key (token_id) references ${getTablePrefix()}token (id),
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Shared links table';
        """
            ) {
                handler.invoke(it)
            }
        }

    override fun addShareLink(
        shareLink: ShareLink,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.updateWithParams(
            """
                INSERT INTO `${getTablePrefix() + tableName}` (`note_id`, `token_id`) VALUES (?, ?)
            """.trimIndent(),
            JsonArray()
                .add(shareLink.noteID)
                .add(shareLink.tokenID)
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }

    override fun deleteByTokenID(
        tokenID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${databaseManager.getTablePrefix() + tableName}` WHERE `token_id` = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(tokenID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getNoteIDByTokenID(
        tokenID: Int,
        sqlConnection: SQLConnection,
        handler: (noteID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `note_id` FROM `${getTablePrefix() + tableName}` WHERE `token_id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(tokenID)) { queryResult ->
            if (queryResult.succeeded()) {
                val row = queryResult.result().results[0]

                handler.invoke(row.getInteger(0), queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun getTokenIDByNoteID(
        noteID: Int,
        sqlConnection: SQLConnection,
        handler: (tokenID: Int?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `token_id` FROM `${getTablePrefix() + tableName}` WHERE `note_id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(noteID)) { queryResult ->
            if (queryResult.succeeded()) {
                val row = queryResult.result().results[0]

                handler.invoke(row.getInteger(0), queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun isLinkExistsByNoteID(
        noteID: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(`id`) FROM `${getTablePrefix() + tableName}` where `note_id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(noteID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }
}