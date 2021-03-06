package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.NoteDao
import com.parnote.db.model.Note
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

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
                constraint ${getTablePrefix()}note_${getTablePrefix()}user_id_fk
                    foreign key (user_id) references ${getTablePrefix()}user (id),
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Note Table';
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
                            "title" to noteInDB.getString(1),
                            "text" to noteInDB.getString(2),
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

    override fun add(
        note: Note,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.updateWithParams(
            """
                INSERT INTO `${getTablePrefix() + tableName}` (user_id, title, text, last_modified, status) VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            JsonArray()
                .add(note.userID)
                .add(note.title)
                .add(note.text)
                .add(System.currentTimeMillis())
                .add(1)
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }

    override fun edit(
        note: Note,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `title` = ?, `text` = ?, `last_modified` = ?, `status` = ? WHERE `id` = ? AND `user_id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(note.title)
                .add(note.text)
                .add(System.currentTimeMillis())
                .add(1)
                .add(note.id)
                .add(note.userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun moveStatus(
        id: Int,
        userID: Int,
        status: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "UPDATE `${getTablePrefix() + tableName}` SET `status` = ? WHERE `id` = ? AND `user_id` = ?"

        sqlConnection.updateWithParams(
            query,
            JsonArray()
                .add(status)
                .add(id)
                .add(userID)
        ) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun delete(
        id: Int,
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${databaseManager.getTablePrefix() + tableName}` WHERE `id` = ? AND `user_id` = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(id).add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun deleteByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "DELETE from `${databaseManager.getTablePrefix() + tableName}` WHERE `user_id` = ?"

        sqlConnection.updateWithParams(query, JsonArray().add(userID)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(Successful(), queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT COUNT(id) FROM `${getTablePrefix() + tableName}` where id = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(id)) { queryResult ->
            if (queryResult.succeeded())
                handler.invoke(queryResult.result().results[0].getInteger(0) == 1, queryResult)
            else
                handler.invoke(null, queryResult)
        }
    }

    override fun getNoteByID(
        noteID: Int,
        sqlConnection: SQLConnection,
        handler: (note: Note?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val query =
            "SELECT `id`, `user_id`, `title`, `text`, `last_modified`, `status`, `favorite` FROM `${getTablePrefix() + tableName}` WHERE `id` = ?"

        sqlConnection.queryWithParams(query, JsonArray().add(noteID)) { queryResult ->
            if (queryResult.succeeded()) {
                val row = queryResult.result().results[0]
                val note = Note(
                    row.getInteger(0),
                    row.getInteger(1),
                    row.getString(2),
                    row.getString(3),
                    row.getString(4),
                    row.getInteger(5),
                    row.getInteger(6) == 1
                )

                handler.invoke(note, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }

    override fun searchByUserID(
        query: String,
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notes: List<Note>?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        val sqlQuery =
            "SELECT `id`, `title`, `text`, `last_modified`, `status`, `favorite` FROM `${getTablePrefix() + tableName}` WHERE (`title` LIKE ? OR `text` LIKE ?) AND `user_id` = ? ORDER BY `id` DESC"

        sqlConnection.queryWithParams(
            sqlQuery,
            JsonArray().add("%$query%").add("%$query%").add(userID)
        ) { queryResult ->
            if (queryResult.succeeded()) {
                val notes = mutableListOf<Note>()

                queryResult.result().results.forEach { noteInDB ->
                    notes.add(
                        Note(
                            noteInDB.getInteger(0),
                            userID,
                            noteInDB.getString(1),
                            noteInDB.getString(2),
                            noteInDB.getString(3),
                            noteInDB.getInteger(4),
                            noteInDB.getInteger(5) == 1
                        )
                    )
                }

                handler.invoke(notes, queryResult)
            } else
                handler.invoke(null, queryResult)
        }
    }
}