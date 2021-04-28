package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.db.model.Note
import com.parnote.db.model.ShareLink
import com.parnote.db.model.Token
import com.parnote.model.*
import com.parnote.util.LoginUtil
import com.parnote.util.TokenUtil
import io.vertx.core.AsyncResult
import io.vertx.ext.web.RoutingContext

class CreateShareLinkAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/shareLink/create")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val noteID = data.getInteger("noteID")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_73))

                return@createConnection
            }

            databaseManager.getDatabase().noteDao.isExistsByID(
                noteID,
                sqlConnection
            ) { exists: Boolean?, _: AsyncResult<*> ->
                if (exists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_66))
                    }

                    return@isExistsByID
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.NOT_EXISTS))
                    }

                    return@isExistsByID
                }

                LoginUtil.getUserIDFromSessionOrCookie(
                    context, sqlConnection, databaseManager
                ) { userID: Int?, _: AsyncResult<*>? ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_67))
                        }

                        return@getUserIDFromSessionOrCookie
                    }

                    databaseManager.getDatabase().noteDao.getNoteByID(
                        noteID,
                        sqlConnection
                    ) { note: Note?, _: AsyncResult<*> ->
                        if (note == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_68))
                            }

                            return@getNoteByID
                        }

                        if (userID != note.userID) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_69))
                            }
                            return@getNoteByID
                        }

                        TokenUtil.createToken(
                            TokenUtil.SUBJECT.SHARE_LINK,
                            userID,
                            databaseManager,
                            sqlConnection
                        ) { token: String?, _: AsyncResult<*> ->
                            if (token == null) {
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_70))
                                }

                                return@createToken
                            }

                            databaseManager.getDatabase().tokenDao.getTokenByToken(
                                token,
                                sqlConnection
                            ) { token: Token?, _: AsyncResult<*> ->
                                if (token == null) {
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_71))
                                    }

                                    return@getTokenByToken
                                }

                                databaseManager.getDatabase().shareLinkDao.addShareLink(
                                    ShareLink(-1, noteID, token.id),
                                    sqlConnection
                                ) { result: Result?, _: AsyncResult<*> ->
                                    databaseManager.closeConnection(sqlConnection) {
                                        if (result == null) {
                                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_72))

                                            return@closeConnection
                                        }

                                        handler.invoke(Successful(mapOf("token" to token.token)))
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



