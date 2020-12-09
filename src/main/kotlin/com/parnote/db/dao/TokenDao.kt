package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.Token
import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface TokenDao : Dao<Token> {
    fun add(token: Token, sqlConnection: SQLConnection, handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit)

    fun getUserIDFromToken(
        token: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun delete(
        token: Token,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isTokenExists(
        token: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isAnyTokenExistByUserIDAndSubject(
        userID: Int,
        subject: String,
        sqlConnection: SQLConnection,
        handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun deleteByUserIDAndSubject(
        userID: Int,
        subject: String,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getCreatedTimeByToken(
        token: String,
        sqlConnection: SQLConnection,
        handler: (createdTime: Long?, asyncResult: AsyncResult<*>) -> Unit
    )
}