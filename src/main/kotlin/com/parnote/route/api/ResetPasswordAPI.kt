package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.db.model.Token
import com.parnote.model.*
import com.parnote.util.TokenUtil
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ResetPasswordAPI : Api() {
    override val routes = arrayListOf("/api/auth/resetPassword")

    override val routeType = RouteType.POST

    init {
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var reCaptcha: ReCaptcha

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val newPassword = data.getString("newPassword")
        val newPasswordRepeat = data.getString("newPasswordRepeat")
        val reCaptcha = data.getString("recaptcha")
        val token = data.getString("token")

        validateForm(newPassword, newPasswordRepeat, reCaptcha, token, handler) {
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_14))

                    return@createConnection
                }

                databaseManager.getDatabase().tokenDao.isTokenExists(token, sqlConnection) { exists, _ ->
                    if (exists == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_16))
                        }

                        return@isTokenExists
                    }

                    if (!exists) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.RESET_PASSWORD_INVALID_TOKEN))
                        }

                        return@isTokenExists
                    }
                    databaseManager.getDatabase().tokenDao.getCreatedTimeByToken(
                        token,
                        sqlConnection
                    ) { createdTime, _ ->
                        if (createdTime == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_19))
                            }

                            return@getCreatedTimeByToken
                        }

                        val thirtyMinInMill = TimeUnit.MINUTES.toMillis(30)

                        if (System.currentTimeMillis() > (createdTime + thirtyMinInMill)) {
                            databaseManager.getDatabase().tokenDao.delete(
                                Token(
                                    -1,
                                    token,
                                    -1,
                                    TokenUtil.SUBJECT.RESET_PASSWORD.toString()
                                ), sqlConnection
                            ) { resultOfDeleteToken, _ ->
                                databaseManager.closeConnection(sqlConnection) {
                                    if (resultOfDeleteToken == null) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_28))

                                        return@closeConnection
                                    }

                                    handler.invoke(Error(ErrorCode.RESET_PASSWORD_INVALID_TOKEN))
                                }
                            }

                            return@getCreatedTimeByToken
                        }

                        databaseManager.getDatabase().tokenDao.getUserIDFromToken(
                            token,
                            sqlConnection
                        ) { userID, _ ->
                            if (userID == null) {
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_21))
                                }

                                return@getUserIDFromToken
                            }

                            databaseManager.getDatabase().userDao.changePasswordByID(
                                userID,
                                newPassword,
                                sqlConnection
                            ) { result, _ ->
                                if (result == null) {
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_23))
                                    }

                                    return@changePasswordByID
                                }

                                databaseManager.getDatabase().tokenDao.delete(
                                    Token(
                                        -1,
                                        token,
                                        -1,
                                        TokenUtil.SUBJECT.RESET_PASSWORD.toString()
                                    ), sqlConnection
                                ) { resultOfDeleteToken, _ ->
                                    databaseManager.closeConnection(sqlConnection) {
                                        if (resultOfDeleteToken == null) {
                                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_65))

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

    private fun validateForm(
        newPassword: String,
        newPasswordRepeat: String,
        reCaptcha: String,
        token: String,
        errorHandler: (result: Result) -> Unit,
        successHandler: () -> Unit
    ) {
        if (token.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.UNKNOWN_ERROR_12))

            return
        }

        if (newPassword.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_NEW_PASSWORD_EMPTY))

            return
        }

        if (newPasswordRepeat.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_NEW_PASSWORD_REPEAT_EMPTY))

            return
        }

        if (newPassword != newPasswordRepeat) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_NEW_PASSWORD_DOESNT_MATCH))

            return
        }

        if (!newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,64}\$"))) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_NEW_PASSWORD_INVALID))

            return
        }

        if (!newPasswordRepeat.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,64}\$"))) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_NEW_PASSWORD_REPEAT_INVALID))

            return
        }

        if (!this.reCaptcha.isValid(reCaptcha)) {
            errorHandler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))

            return
        }

        successHandler.invoke()
    }
}


