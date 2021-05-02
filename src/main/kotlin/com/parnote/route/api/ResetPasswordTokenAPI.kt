package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import io.vertx.ext.web.RoutingContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ResetPasswordTokenAPI : Api() {

    override val routes = arrayListOf("/api/token/check/resetPassword")

    override val routeType = RouteType.POST

    init {
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val token = data.getString("token")

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_25))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.getCreatedTimeByToken(token, sqlConnection) { createdTime, _ ->
                databaseManager.closeConnection(sqlConnection) {
                    if (createdTime == null) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_11))

                        return@closeConnection
                    }

                    val minutesInMills = TimeUnit.MINUTES.toMillis(30) // 30 minutes

                    if (System.currentTimeMillis() > (createdTime + minutesInMills)) {
                        handler.invoke(Error(ErrorCode.EXPIRED_TOKEN_VALIDATION))

                        return@closeConnection
                    }

                    handler.invoke(Successful())
                }
            }
        }
    }
}



