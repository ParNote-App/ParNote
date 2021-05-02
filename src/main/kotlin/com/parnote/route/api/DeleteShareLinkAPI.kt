package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.db.model.Note
import com.parnote.db.model.Token
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext

class DeleteShareLinkAPI : LoggedInApi() {
    override val routes: ArrayList<String> = arrayListOf("/api/shareLink/delete")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val token = data.getString("token")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_13))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(token, sqlConnection) { exists, _ ->
                if (exists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                    }

                    return@isTokenExists
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.NOT_EXIST))
                    }

                    return@isTokenExists
                }

                databaseManager.getDatabase().tokenDao.getTokenByToken(
                    token,
                    sqlConnection
                ) { token: Token?, _: AsyncResult<*> ->
                    if (token == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                        }

                        return@getTokenByToken
                    }

                    LoginUtil.getUserIDFromSessionOrCookie(
                        context,
                        sqlConnection,
                        databaseManager
                    ) { userID: Int?, _: AsyncResult<*>? ->
                        if (userID == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                            }

                            return@getUserIDFromSessionOrCookie
                        }

                        databaseManager.getDatabase().shareLinkDao.getNoteIDByTokenID(
                            token.id,
                            sqlConnection
                        ) { noteID: Int?, _: AsyncResult<*> ->
                            if (noteID == null) {
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                                }

                                return@getNoteIDByTokenID
                            }

                            databaseManager.getDatabase().noteDao.getNoteByID(
                                noteID,
                                sqlConnection
                            ) { note: Note?, _: AsyncResult<*> ->
                                if (note == null) {
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                                    }

                                    return@getNoteByID
                                }

                                if (userID != note.userID) {
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                                    }

                                    return@getNoteByID
                                }

                                databaseManager.getDatabase().shareLinkDao.deleteByTokenID(
                                    token.id,
                                    sqlConnection
                                ) { result: Result?, _: AsyncResult<*> ->
                                    if (result == null) {
                                        databaseManager.closeConnection(sqlConnection) {
                                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))
                                        }

                                        return@deleteByTokenID
                                    }

                                    databaseManager.getDatabase().tokenDao.deleteByID(
                                        token.id,
                                        sqlConnection
                                    ) { result: Result?, _: AsyncResult<*> ->
                                        databaseManager.closeConnection(sqlConnection) {
                                            if (result == null) {
                                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))

                                                return@closeConnection
                                            }

                                            handler.invoke(Successful())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}