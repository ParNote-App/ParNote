package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.db.model.Token
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.core.AsyncResult
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

                        val mutableNotes = mutableListOf<MutableMap<String, Any?>>()

                        convertedNotes.forEach { note ->
                            mutableNotes.add(note.toMutableMap())
                        }

                        val handlers: List<(handler: () -> Unit) -> Any> =
                            mutableNotes.map { note ->
                                val localHandler: (handler: () -> Unit) -> Any = { localHandler ->
                                    databaseManager.getDatabase().shareLinkDao.isLinkExistsByNoteID(
                                        note["id"] as Int,
                                        sqlConnection
                                    ) { exists: Boolean?, _: AsyncResult<*> ->
                                        if (exists == null) {
                                            databaseManager.closeConnection(sqlConnection) {
                                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_83))
                                            }

                                            return@isLinkExistsByNoteID
                                        }

                                        note["shared"] = exists

                                        if (!exists) {
                                            note["sharedToken"] to ""

                                            localHandler.invoke()

                                            return@isLinkExistsByNoteID
                                        }

                                        databaseManager.getDatabase().shareLinkDao.getTokenIDByNoteID(
                                            note["id"] as Int,
                                            sqlConnection
                                        ) { tokenID: Int?, _: AsyncResult<*> ->
                                            if (tokenID == null) {
                                                databaseManager.closeConnection(sqlConnection) {
                                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_84))
                                                }

                                                return@getTokenIDByNoteID
                                            }

                                            databaseManager.getDatabase().tokenDao.getTokenByTokenID(
                                                tokenID,
                                                sqlConnection
                                            ) { token: Token?, _: AsyncResult<*> ->
                                                if (token == null) {
                                                    databaseManager.closeConnection(sqlConnection) {
                                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_85))
                                                    }

                                                    return@getTokenByTokenID
                                                }

                                                note["sharedToken"] to token.token

                                                localHandler.invoke()
                                            }
                                        }
                                    }
                                }

                                localHandler
                            }

                        var currentIndex = -1

                        fun invoke() {
                            val localHandler: () -> Unit = {
                                if (currentIndex == handlers.lastIndex)
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(
                                            Successful(
                                                mapOf(
                                                    "notes" to mutableNotes
                                                )
                                            )
                                        )
                                    }
                                else
                                    invoke()
                            }

                            currentIndex++

                            if (currentIndex <= handlers.lastIndex)
                                handlers[currentIndex].invoke(localHandler)
                        }

                        invoke()
                    }
                }
            }
        }
    }
}



