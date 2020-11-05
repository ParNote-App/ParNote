package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main.Companion.getComponent
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import com.parnote.util.LoginUtil
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject


class LoginAPI : Api() {
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
        val reCaptcha = data.getString("recaptcha")

        validateForm(usernameOrEmail, password,reCaptcha, handler) {
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_5))
                    return@createConnection
                }

                databaseManager.getDatabase().userDao.isExistsByUsernameOrEmail(
                    usernameOrEmail,
                    sqlConnection
                ) { exists, _ ->
                    if (exists == null) {
                        databaseManager.closeConnection(sqlConnection){
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_9))
                        }

                        return@isExistsByUsernameOrEmail
                    }
                    if (!exists){
                        databaseManager.closeConnection(sqlConnection){
                            handler.invoke(Error(ErrorCode.LOGIN_IS_INVALID))
                        }

                        return@isExistsByUsernameOrEmail
                    }

                    databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
                        usernameOrEmail,
                        sqlConnection
                    ) { userID, _ ->
                        if (userID == null) {
                            databaseManager.closeConnection(sqlConnection){
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_7))
                            }

                            return@getUserIDFromUsernameOrEmail
                        }

                        databaseManager.getDatabase().userDao.isEmailVerifiedByID(
                            userID,
                            sqlConnection
                        ) { isVerified, _ ->
                            if (isVerified == null) {
                                databaseManager.closeConnection(sqlConnection){
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_8))
                                }

                                return@isEmailVerifiedByID
                            }
                            if (!isVerified){
                                databaseManager.closeConnection(sqlConnection){
                                    handler.invoke(Error(ErrorCode.LOGIN_EMAIL_NOT_VERIFIED))
                                }

                                return@isEmailVerifiedByID
                            }
                            LoginUtil.login(
                                usernameOrEmail,
                                password,
                                rememberMe,
                                context,
                                databaseManager,
                                sqlConnection
                            ) { isLoggedIn, _ ->
                                databaseManager.closeConnection(sqlConnection) {
                                    if (isLoggedIn == null) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_6))
                                        return@closeConnection
                                    }
                                    if (isLoggedIn) {
                                        handler.invoke(Successful())
                                    } else {
                                        handler.invoke(Error(ErrorCode.LOGIN_IS_INVALID))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun validateForm(
        usernameOrEmail: String,
        password: String,
        reCaptcha: String,
        errorHandler: (result: Result) -> Unit,
        successHandler: () -> Unit
    ) {

        if (usernameOrEmail.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.LOGIN_USERNAME_OR_EMAIL_INVALID))
            return
        }

        if (!usernameOrEmail.matches(Regex("^[a-zA-Z0-9]+\$")) && !usernameOrEmail.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            errorHandler.invoke(Error(ErrorCode.LOGIN_USERNAME_OR_EMAIL_INVALID))
            return
        }

        if (password.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.LOGIN_PASSWORD_INVALID))
        }

        if (!this.reCaptcha.isValid(reCaptcha)) {
            errorHandler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))
            return
        }


        successHandler.invoke()


    }
}