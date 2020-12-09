package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.Note
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface NoteDao : Dao<Note> {
    fun getNotesByUserID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (notes: List<Map<String, Any>>?, asyncResult: AsyncResult<*>) -> Unit
    )
}