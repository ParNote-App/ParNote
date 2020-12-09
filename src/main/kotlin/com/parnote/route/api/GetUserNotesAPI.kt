package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class GetUserNotesAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/getNotes")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_32))

                return@createConnection
            }

            LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_33))
                    }

                    return@getUserIDFromSessionOrCookie
                }

                databaseManager.getDatabase().noteDao.getNotesByUserID(userID, sqlConnection) { notes, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (notes == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_34))

                            return@closeConnection
                        }

                        handler.invoke(
                            Successful(
                                mapOf(
                                    "notes" to notes
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}