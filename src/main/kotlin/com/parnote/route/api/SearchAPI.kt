package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class SearchAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/notes/search")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (Result) -> Unit) {
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
                ) { notes, _ ->
                    databaseManager.closeConnection(sqlConnection) {
                        if (notes == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_74))

                            return@closeConnection
                        }

                        val convertedNotes = mutableListOf<Map<String, Any?>>()

                        notes.forEach {
                            convertedNotes.add(
                                mapOf(
                                    "id" to it.id,
                                    "title" to it.title,
                                    "text" to it.text,
                                    "last_modified" to it.lastModified,
                                    "status" to it.status,
                                    "favorite" to it.favorite
                                )
                            )
                        }

                        handler.invoke(
                            Successful(
                                mapOf(
                                    "notes" to convertedNotes,
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}



