package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext

class SettingsChangePasswordAPI : LoggedInApi() {
    override val routes = arrayListOf("/api/user/settings/changePassword")

    override val routeType = RouteType.POST

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val currentPassword = data.getString("currentPassword")
        val newPassword = data.getString("newPassword")
        val newPasswordRepeat = data.getString("newPasswordRepeat")

        validateForm(currentPassword, newPassword, newPasswordRepeat, handler) {
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_55))

                    return@createConnection
                }

                LoginUtil.getUserIDFromSessionOrCookie(context, sqlConnection, databaseManager) { userID, _ ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_56))
                        }

                        return@getUserIDFromSessionOrCookie
                    }

                    databaseManager.getDatabase().userDao.isCorrectPasswordByUserID(
                        userID,
                        currentPassword,
                        sqlConnection
                    ) { isCorrect, _ ->
                        if (isCorrect == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_57))
                            }

                            return@isCorrectPasswordByUserID
                        }

                        if (!isCorrect) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_INVALID_PASSWORD))
                            }

                            return@isCorrectPasswordByUserID
                        }

                        databaseManager.getDatabase().userDao.changePasswordByID(
                            userID,
                            newPassword,
                            sqlConnection
                        ) { result, _ ->
                            databaseManager.closeConnection(sqlConnection) {
                                if (result == null) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_58))

                                    return@closeConnection
                                }

                                if (result is Successful)
                                    handler.invoke(Successful())
                                else
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_59))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun validateForm(
        currentPassword: String,
        newPassword: String,
        newPasswordRepeat: String,
        errorHandler: (result: Result) -> Unit,
        successHandler: () -> Unit
    ) {
        if (currentPassword.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_INVALID_PASSWORD))

            return
        }

        if (newPassword.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_NEW_PASSWORD_EMPTY))

            return
        }

        if (newPasswordRepeat.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_NEW_PASSWORD_REPEAT_EMPTY))

            return
        }

        if (newPassword != newPasswordRepeat) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_NEW_PASSWORD_DOESNT_MATCH))

            return
        }

        if (!newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,64}\$"))) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_NEW_PASSWORD_INVALID))

            return
        }

        if (!newPasswordRepeat.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,64}\$"))) {
            errorHandler.invoke(Error(ErrorCode.SETTINGS_CHANGE_PASSWORD_NEW_PASSWORD_REPEAT_INVALID))

            return
        }

        successHandler.invoke()
    }
}