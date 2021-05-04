package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.core.AsyncResult
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
                    if (notes == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_34))
                        }

                        return@getNotesByUserID
                    }

                    val mutableNotes = mutableListOf<MutableMap<String, Any?>>()

                    notes.forEach { note ->
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

                                    localHandler.invoke()
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