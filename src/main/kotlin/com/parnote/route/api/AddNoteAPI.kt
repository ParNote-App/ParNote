package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.db.model.Note
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class AddNoteAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/addNote")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val title = data.getString("title")
        val note = data.getString("text")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_35))

                return@createConnection
            }

            LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_36))
                    }

                    return@getUserIDFromSessionOrCookie
                }

                databaseManager.getDatabase().noteDao.add(
                    Note(-1, userID, title, note, "", 1, false),
                    sqlConnection
                ) { result, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (result == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_37))

                            return@closeConnection
                        }

                        handler.invoke(
                            if (result is Successful)
                                Successful()
                            else
                                Error(ErrorCode.UNKNOWN_ERROR_38)
                        )
                    }
                }
            }
        }
    }
}