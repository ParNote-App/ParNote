package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.model.Api
import com.parnote.model.Error
import com.parnote.model.Result
import com.parnote.model.RouteType
import com.parnote.model.Successful
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject


class ResetPasswordPageAPI: Api() {
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
        val reCaptcha = data.getString("reCaptcha")

        validateForm(newPassword, newPasswordRepeat, reCaptcha, handler){
        }

        handler.invoke(Successful())

        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))
    }

    fun validateForm(newPassword: String, newPasswordRepeat: String, reCaptcha: String,
                     errorHandler: (result: Result) -> Unit, successHandler: () -> Unit) {

        //if (name)

        //if (surname)

        if(newPassword.isEmpty()){
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_EMPTY))
            return
        }

        if (!newPassword.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"))){
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_INVALID))
            return
        }

        if(newPasswordRepeat.isEmpty()){
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_REPEAT_EMPTY))
            return
        }

        if (!newPasswordRepeat.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}\$"))){
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_REPEAT_INVALID))
            return
        }

        if (newPassword != newPasswordRepeat){
            errorHandler.invoke(Error(ErrorCode.NEWPASSWORD_DOESNT_MATCH))
            return
        }

        if (!this.reCaptcha.isValid(reCaptcha)) {
            errorHandler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))
            return
        }



        successHandler.invoke()
    }

    }


