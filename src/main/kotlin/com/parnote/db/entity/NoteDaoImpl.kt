package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.NoteDao
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection
import java.util.*

class NoteDaoImpl(override val tableName: String = "note") : DaoImpl(), NoteDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int(11) NOT NULL,
              `title` MEDIUMTEXT NOT NULL,
              `text` MEDIUMTEXT NOT NULL,
              `last_modified` MEDIUMTEXT NOT NULL,
              `status` int(1) NOT NULL DEFAULT 0,
              `favorite` int(1) NOT NULL DEFAULT 0,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Note Table';
        """
            ) {
                handler.invoke(it)
            }
        }

    override fun getNotesByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notes: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `title`, `text`, `last_modified`, `status`, `favorite` FROM `${getTablePrefix() + tableName}` WHERE `user_id` = ? ORDER BY `id` DESC"

        sqlConnection.queryWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded()) {
                val notes = mutableListOf<Map<String, Any>>()

                queryResult.result().results.forEach { noteInDB ->
                    notes.add(
                        mapOf(
                            "id" to noteInDB.getInteger(0),
                            "title" to String(
                                Base64.getDecoder().decode(noteInDB.getString(1).toByteArray())
                            ),
                            "text" to String(
                                Base64.getDecoder().decode(noteInDB.getString(2).toByteArray())
                            ),
                            "last_modified" to noteInDB.getString(3),
                            "status" to noteInDB.getInteger(4),
                            "favorite" to noteInDB.getInteger(5)
                        )
                    )
                }

                handler.invoke(notes, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }
}