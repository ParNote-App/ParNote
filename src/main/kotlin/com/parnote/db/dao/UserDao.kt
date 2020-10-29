package com.parnote.db.dao

import com.parnote.db.Dao
import com.parnote.db.model.User
import com.parnote.model.Result
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
interface UserDao : Dao<User> {
    fun isEmailExists(
            email: String,
            sqlConnection: SQLConnection,
            handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isUsernameExists(
            username: String,
            sqlConnection: SQLConnection,
            handler: (exists: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isLoginCorrect(
        usernameOrEmail: String,
        password: String,
        sqlConnection: SQLConnection,
        handler: (loginCorrect: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun add(
        user: User,
        sqlConnection: SQLConnection,
        handler: (isSuccessful: Result?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getSecretKeyByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (secretKey: String?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun getUserIDFromUsernameOrEmail(
        usernameOrEmail: String,
        sqlConnection: SQLConnection,
        handler: (userID: Int?, asyncResult: AsyncResult<*>) -> Unit
    )

    fun isEmailVerifiedByID(
        userID: Int,
        sqlConnection: SQLConnection,
        handler: (isVerified: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    )
}
