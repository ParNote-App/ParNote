package com.parnote.route.api


import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class EmailVerificationAPI : Api() {
    override val routes = arrayListOf("/api/auth/emailVerification")

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
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_7))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(token, sqlConnection) { exists, _ ->
                if (exists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_3))
                    }

                    return@isTokenExists
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.EMAIL_VERIFICATION_INVALID_TOKEN))
                    }

                    return@isTokenExists
                }

                databaseManager.getDatabase().tokenDao.getUserIDFromToken(token, sqlConnection) { userID, _ ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_4))
                        }

                        return@getUserIDFromToken
                    }

                    databaseManager.getDatabase().userDao.makeEmailVerifiedByID(userID, sqlConnection) { result, _ ->
                        databaseManager.closeConnection(sqlConnection) {
                            if (result == null) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_9))

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









