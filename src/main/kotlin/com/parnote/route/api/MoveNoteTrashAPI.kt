package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class MoveNoteTrashAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/moveNoteTrash")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val id = data.getInteger("id")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_43))

                return@createConnection
            }

            LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_44))
                    }

                    return@getUserIDFromSessionOrCookie
                }

                databaseManager.getDatabase().noteDao.moveStatus(
                    id,
                    userID,
                    3,
                    sqlConnection
                ) { result, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (result == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_45))

                            return@closeConnection
                        }

                        handler.invoke(
                            if (result is Successful)
                                Successful()
                            else
                                Error(ErrorCode.UNKNOWN_ERROR_46)
                        )
                    }
                }
            }
        }
    }
}