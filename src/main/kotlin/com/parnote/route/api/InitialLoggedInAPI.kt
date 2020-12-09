package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class InitialLoggedInAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/loggedIn/initialData")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_29))

                return@createConnection
            }

            LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_30))
                    }

                    return@getUserIDFromSessionOrCookie
                }

                databaseManager.getDatabase().userDao.getUser(userID, sqlConnection) { user, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (user == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_31))

                            return@closeConnection
                        }

                        handler.invoke(
                            Successful(
                                mapOf(
                                    "name" to user.name,
                                    "surname" to user.surname,
                                    "username" to user.username,
                                    "email" to user.email
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}