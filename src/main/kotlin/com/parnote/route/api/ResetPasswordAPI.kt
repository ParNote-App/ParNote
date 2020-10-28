package com.parnote.route.api


import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.*
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

        val usernameOrEmail = data.getString("usernameOrEmail")
        val reCaptcha = data.getString("recaptcha")


        validateForm(usernameOrEmail, reCaptcha, handler) {

        }

        handler.invoke(Successful())



        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))

    }


    fun validateForm (usernameOrEmail: String, reCaptcha: String, errorHandler : (result: Result) -> Unit, successHandler: () -> Unit
        ) {

        if (usernameOrEmail.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID))
            return
        }



        if (!usernameOrEmail.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")) && !usernameOrEmail.matches(Regex("^[a-zA-Z0-9]+\$")) ) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID))
            return
        }


        if(this.reCaptcha.isValid(reCaptcha)){
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_RECAPTCHA_INVALID))
            return
        }

        successHandler.invoke()
    }



}
