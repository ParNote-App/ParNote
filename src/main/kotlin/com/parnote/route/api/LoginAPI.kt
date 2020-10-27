package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main.Companion.getComponent
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject
import kotlin.Error


class LoginAPI: Api() {
    override val routes: ArrayList<String> = arrayListOf("/api/auth/loginAPI")

    override val routeType = RouteType.POST


    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var reCaptcha: ReCaptcha



    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val usernameOrEmail = data.getString("usernameOrEmail")
        val password = data.getString("password")
        val rememberMe = data.getBoolean("rememberMe")

        validateForm(usernameOrEmail,password, handler) {

        }

        handler.invoke(Successful())

        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))
    }

    fun validateForm(usernameOrEmail: String, password: String, errorHandler: (result: Result) -> Unit, successHandler: () -> Unit){

        if (usernameOrEmail.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.LOGIN_USERNAME_OR_EMAIL_INVALID))
            return
        }

        if (!usernameOrEmail.matches(Regex("^[a-zA-Z0-9]+\$")) && !usernameOrEmail.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            errorHandler.invoke(Error(ErrorCode.LOGIN_USERNAME_OR_EMAIL_INVALID))
            return
        }

        if (password.isEmpty()){
            errorHandler.invoke(Error(ErrorCode.LOGIN_PASSWORD_INVALID))
        }

        successHandler.invoke()


    }
}