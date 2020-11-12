package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject


class ResetPasswordPageAPI : Api() {
    override val routes = arrayListOf("/api/auth/resetPasswordPageAPI")


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

        val newPassword = data.getString("resetPassword")
        val newPasswordRepeat = data.getString("resetPassword")

        validateForm(newPassword, newPasswordRepeat, handler) {
        }

        handler.invoke(Successful())

        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_12))
    }

    fun validateForm(
        newPassword: String, newPasswordRepeat: String,
        errorHandler: (result: Result) -> Unit, successHandler: () -> Unit
    ) {
        if (newPassword.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_EMPTY))
            return
        }

        if (!newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"))) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_INVALID))
            return
        }

        if (newPasswordRepeat.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_EMPTY))
            return
        }

        if (!newPasswordRepeat.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"))) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_INVALID))
            return
        }

        if (newPassword != newPasswordRepeat) {
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_DOESNT_MATCH))
            return
        }

        successHandler.invoke()
    }

}


