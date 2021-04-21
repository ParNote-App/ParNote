package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.Note
import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface NoteDao : Dao<Note> {
    fun getNotesByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notes: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun add(
        note: Note,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun edit(
        note: Note,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun moveStatus(
        id: Int,
        userID: Int,
        status: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(
        id: Int,
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun deleteByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isExistsByID(
        id: Int,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getNoteByID(
        noteID: Int,
        sqlConnection: SQLConnection,
        handler: (note: Note?, asyncResult: AsyncResult<*>) -> Unit
    )
}