package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext



class SearchAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/auth/searchApi")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (com.parnote.model.Result) -> Unit) {
        val data = context.bodyAsJson

        val query = data.getString("query")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_73))


                return@createConnection
            }


            LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                if (userID == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_72))
                    }

                    return@getUserIDFromSessionOrCookie

                }

                databaseManager.getDatabase().noteDao.searchByUserID(
                    query,
                    userID,
                    sqlConnection
                ) { notes, asyncResult ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (notes == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_74))
                        }

                        return@closeConnection
                    }

                    handler.invoke(
                        Successful(
                            mapOf(
                                "note" to notes,

                                )
                        )
                    )
                }
            }
        }


    }
}



