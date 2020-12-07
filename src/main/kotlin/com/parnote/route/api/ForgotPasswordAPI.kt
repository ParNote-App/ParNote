package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.config.ConfigManager
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import com.parnote.util.MailUtil
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.mail.MailClient
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import javax.inject.Inject

class ForgotPasswordAPI : Api() {
    override val routes = arrayListOf("/api/auth/forgotPassword")

    override val routeType = RouteType.POST

    init {
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    @Inject
    lateinit var reCaptcha: ReCaptcha

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var mailClient: MailClient

    @Inject
    lateinit var templateEngine: HandlebarsTemplateEngine

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        val data = context.bodyAsJson

        val usernameOrEmail = data.getString("usernameOrEmail")
        val reCaptcha = data.getString("recaptcha")

        validateForm(usernameOrEmail, reCaptcha, handler) {
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) {
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_24))

                    return@createConnection
                }

                databaseManager.getDatabase().userDao.isExistsByUsernameOrEmail(
                    usernameOrEmail,
                    sqlConnection
                ) { exists, _ ->
                    if (exists == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_2))
                        }

                        return@isExistsByUsernameOrEmail
                    }

                    if (!exists) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID))
                        }

                        return@isExistsByUsernameOrEmail
                    }

                    databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
                        usernameOrEmail,
                        sqlConnection
                    ) { userID, _ ->
                        if (userID == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_18))
                            }

                            return@getUserIDFromUsernameOrEmail
                        }

                        MailUtil.sendMail(
                            userID,
                            MailUtil.MailType.RESET_PASSWORD,
                            MailUtil.LangType.EN, // TODO get lang from remote
                            sqlConnection,
                            templateEngine,
                            configManager,
                            databaseManager,
                            mailClient
                        ) { result, _ ->
                            databaseManager.closeConnection(sqlConnection) {
                                if (result == null) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_20))

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

    private fun validateForm(
        usernameOrEmail: String,
        reCaptcha: String,
        errorHandler: (result: Result) -> Unit,
        successHandler: () -> Unit
    ) {
        if (usernameOrEmail.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID))

            return
        }

        if (
            !usernameOrEmail.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$")) && // user email regex
            !usernameOrEmail.matches(Regex("^[a-zA-Z0-9_]+\$")) // username regex
        ) {
            errorHandler.invoke(Error(ErrorCode.RESET_PASSWORD_USERNAME_OR_EMAIL_INVALID))

            return
        }

        if (!this.reCaptcha.isValid(reCaptcha)) {
            errorHandler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))

            return
        }

        successHandler.invoke()
    }
}