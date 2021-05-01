package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import io.vertx.ext.web.RoutingContext

class GetShareLinkAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/shareLink/get")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val token = data.getString("token")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_70))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(token, sqlConnection) { exists, _ ->
                if (exists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_66))
                    }

                    return@isTokenExists
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.NOT_EXIST))
                    }

                    return@isTokenExists
                }

                databaseManager.getDatabase().tokenDao.getTokenByToken(token, sqlConnection) { token, _ ->
                    if (token == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_67))
                        }

                        return@getTokenByToken
                    }

                    databaseManager.getDatabase().shareLinkDao.getNoteIDByTokenID(
                        token.id,
                        sqlConnection
                    ) { noteID, _ ->
                        if (noteID == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_68))
                            }

                            return@getNoteIDByTokenID
                        }

                        databaseManager.getDatabase().noteDao.getNoteByID(noteID, sqlConnection) { note, _ ->
                            if (note == null) {
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_69))
                                }

                                return@getNoteByID
                            }

                            handler.invoke(
                                Successful(
                                    mapOf(
                                        "note" to note,
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}