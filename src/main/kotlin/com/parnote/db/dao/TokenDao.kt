package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.Token
import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TokenDao : Dao<Token> {
    fun add(token: Token, sqlConnection: SQLConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)
}