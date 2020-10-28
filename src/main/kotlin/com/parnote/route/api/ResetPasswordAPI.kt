package com.parnote.route.api


import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.Api
import com.parnote.model.Error
import com.parnote.model.Result
import com.parnote.model.RouteType
import de.triology.recaptchav2java.ReCaptcha

import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class ResetPasswordAPI: Api() {

    override val routes = arrayListOf("/api/auth/ResetPasswordAPI")

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

        val emailOrUsername = data.getString("emailOrUsername")
        val password = data.getString("password")
        val forgotPassword = data.getBoolean("forgotPassword?")

        validateForm(emailOrUsername, password, forgotPassword, handler) {

        }



        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))

    }


    fun validateForm (emailOrUsername: String, password: String, forgotPassword: Boolean,  errorHandler : (result: Result) -> Unit, successHandler: () -> Unit
        ) {

        if (password.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.RESETPASSWORD_PASSWORD_EMPTY))
            return
        }

        if (password.length < 5) {
            errorHandler.invoke(Error(ErrorCode.RESETPASSWORD_PASSWORD_SHORT))
            return
        }

        if (password.length > 30){
            errorHandler.invoke(Error(ErrorCode.RESETPASSWORD_PASSWORD_LONG))

        }

        if (!password.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            errorHandler.invoke(Error(ErrorCode.RESETPASSWORD_PASSWORD_INVALID))
            return
        }

        successHandler.invoke()
    }



}
