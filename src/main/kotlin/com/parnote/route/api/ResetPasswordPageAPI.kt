package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class ResetPasswordPageAPI : Api() {
    override val routes = arrayListOf("/api/auth/resetPasswordPageAPI")

    override val routeType = RouteType.POST

    init {
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager //db manageri cagirdik

    @Inject
    lateinit var reCaptcha: ReCaptcha //ReCaptchayi cagirdik

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val newPassword = data.getString("newPassword")
        val newPasswordRepeat = data.getString("newPasswordRepeat")
        val reCaptcha = data.getString("recaptcha")
        val token = data.getString("token")

        validateForm(newPassword, newPasswordRepeat, reCaptcha, token, handler) {
            databaseManager.createConnection { sqlConnection, asyncResult ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_3))
                    return@createConnection
                }
                databaseManager.getDatabase().tokenDao.isTokenExists(token, sqlConnection) { exists, asyncResult ->
                    if (exists == null) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_4))
                        return@isTokenExists
                    }
                    if (!exists) {
                        handler.invoke(Error(ErrorCode.TOKEN_IS_INVALID))
                        return@isTokenExists
                    }
                    databaseManager.getDatabase().tokenDao.getCreatedTimeByToken(
                        token,
                        sqlConnection
                    ) { createdTime, asyncResult ->
                        if (createdTime == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_5))
                            return@getCreatedTimeByToken
                        }

                        val thirtyMinInMill = TimeUnit.MINUTES.toMillis(30)

                        if (System.currentTimeMillis() > (createdTime + thirtyMinInMill)) {
                            handler.invoke(Error(ErrorCode.TOKEN_IS_INVALID))
                            return@getCreatedTimeByToken
                        }


                    }
                }
            }

        }

//        handler.invoke(Successful())
//
//        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))
    }

    fun validateForm(
        newPassword: String,
        newPasswordRepeat: String,
        reCaptcha: String,
        token: String,
        errorHandler: (result: Result) -> Unit, successHandler: () -> Unit
    ) {
        // token null check > UNKNOWN_ERROR_ bir tane de numara
        if (token.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.UNKNOWN_ERROR_2))
            return
        }

        if (newPassword.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_EMPTY))

            return
        }

        if (newPasswordRepeat.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_REPEAT_EMPTY))

            return
        }

        if (newPassword != newPasswordRepeat) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_DOESNT_MATCH))

            return
        }

        if (!newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"))) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_INVALID))

            return
        }

        if (!newPasswordRepeat.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"))) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_REPEAT_INVALID))

            return
        }

        if (!this.reCaptcha.isValid(reCaptcha)) {
            errorHandler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))

            return
        }

        successHandler.invoke()
    }
}


