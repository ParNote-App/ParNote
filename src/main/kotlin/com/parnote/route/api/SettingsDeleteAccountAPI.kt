package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class SettingsDeleteAccountAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/settings/deleteAccount")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val confirmation = data.getBoolean("confirm")

        validateForm(confirmation, handler) {
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_60))

                    return@createConnection
                }

                LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_61))
                        }

                        return@getUserIDFromSessionOrCookie
                    }

                    databaseManager.getDatabase().noteDao.deleteByUserID(userID, sqlConnection) { result, _ ->
                        if (result === null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_62))
                            }

                            return@deleteByUserID
                        }

                        databaseManager.getDatabase().userDao.deleteByUserID(userID, sqlConnection) { result, _ ->
                            if (result === null) {
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_63))
                                }

                                return@deleteByUserID
                            }

                            databaseManager.getDatabase().tokenDao.deleteByUserID(userID, sqlConnection) { result, _ ->
                                if (result === null) {
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_64))
                                    }

                                    return@deleteByUserID
                                }

                                LoginUtil.logout(databaseManager, context) { _, _ ->
                                    databaseManager.closeConnection(sqlConnection) {
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

    private fun validateForm(
        confirmation: Boolean,
        errorHandler: (result: Result) -> Unit,
        successHandler: () -> Unit
    ) {
        if (!confirmation) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_DELETE_ACCOUNT_DIDNT_CHECK_CHECKBOX))

            return
        }

        successHandler.invoke()
    }
}