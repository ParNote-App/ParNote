package com.parnote.util

import com.parnote.db.DatabaseManager
import io.vertx.core.AsyncResult
import io.vertx.core.http.Cookie
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext

object LoginUtil {
    const val SESSION_COOKIE_NAME = "parnote_token"

    fun login(
        usernameOrEmail: String,
        password: String,
        rememberMe: Boolean,
        routingContext: RoutingContext,
        databaseManager: DatabaseManager,
        sqlConnection: SQLConnection,
        handler: (isLoggedIn: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.isLoginCorrect(
            usernameOrEmail,
            password,
            sqlConnection
        ) { loginCorrect, asyncResult ->
            if (loginCorrect == null) {
                handler.invoke(null, asyncResult)

                return@isLoginCorrect
            }

            if (loginCorrect) {
                databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
                    usernameOrEmail,
                    sqlConnection
                ) { userID, asyncResultOfUserID ->
                    if (userID == null) {
                        handler.invoke(null, asyncResultOfUserID)

                        return@getUserIDFromUsernameOrEmail
                    }

                    if (rememberMe)
                        TokenUtil.createToken(
                            TokenUtil.SUBJECT.LOGIN_SESSION,
                            userID,
                            databaseManager,
                            sqlConnection
                        ) { token, asyncResultOfCreateToken ->
                            if (token == null) {
                                handler.invoke(null, asyncResultOfCreateToken)

                                return@createToken
                            }

                            val age = 60 * 60 * 24 * 365 * 2L // 2 years valid
                            val path = "/" // root dir

                            val tokenCookie = Cookie.cookie(SESSION_COOKIE_NAME, token)

                            tokenCookie.setMaxAge(age)
                            tokenCookie.path = path

                            routingContext.addCookie(tokenCookie)

                            handler.invoke(true, asyncResultOfCreateToken)
                        }
                    else {
                        routingContext.session().put("user_id", userID)


                        handler.invoke(true, asyncResult)
                    }
                }

                return@isLoginCorrect
            }

            handler.invoke(false, asyncResult)
        }
    }
}