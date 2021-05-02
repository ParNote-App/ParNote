package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class MoveNoteArchiveAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/moveNoteArchive")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val id = data.getInteger("id")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_47))

                return@createConnection
            }

            LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_48))
                    }

                    return@getUserIDFromSessionOrCookie
                }

                databaseManager.getDatabase().noteDao.moveStatus(
                    id,
                    userID,
                    2,
                    sqlConnection
                ) { result, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (result == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_49))

                            return@closeConnection
                        }

                        handler.invoke(
                            if (result is Successful)
                                Successful()
                            else
                                Error(ErrorCode.UNKNOWN_ERROR_50)
                        )
                    }
                }
            }
        }
    }
}