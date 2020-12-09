package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.db.model.Note
import com.parnote.model.*
import io.vertx.ext.web.RoutingContext

class EditNoteAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/editNote")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val id = data.getInteger("id")
        val title = data.getString("title")
        val note = data.getString("text")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_39))

                return@createConnection
            }

            databaseManager.getDatabase().noteDao.edit(
                Note(id, -1, title, note, "", 1, false),
                sqlConnection
            ) { result, _ ->
                databaseManager.closeConnection(sqlConnection) {
                    if (result == null) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_40))

                        return@closeConnection
                    }

                    handler.invoke(
                        if (result is Successful)
                            Successful()
                        else
                            Error(ErrorCode.UNKNOWN_ERROR_41)
                    )
                }
            }
        }
    }
}