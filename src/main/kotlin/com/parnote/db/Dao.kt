package com.parnote.db

import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

interface Dao<T> {
    fun init(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection

    fun add(t: T, handler: (result: Result) -> Unit)
}