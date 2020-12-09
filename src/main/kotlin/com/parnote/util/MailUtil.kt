package com.parnote.util

import com.parnote.config.ConfigManager
import com.parnote.db.DatabaseManager
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonObject
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine

object MailUtil {
    enum class MailType(
        val tokenSubject: TokenUtil.SUBJECT,
        val tokenAddress: String,
        val templatePath: String,
        val templateParametersByLanguage: Map<LangType, Map<String, Any>>
    ) {
        ACTIVATION(
            TokenUtil.SUBJECT.VERIFY_MAIL,
            "/activate?token=%s",
            "view/mail/VerifyEmailMail.hbs",
            mapOf<LangType, Map<String, Any>>(
                LangType.TR to mapOf<String, Any>(
                    "subject" to "E-Mail Aktivasyonu",
                    "template-params" to JsonObject()
                ),
                LangType.EN to mapOf<String, Any>(
                    "subject" to "E-Mail Verification",
                    "template-params" to JsonObject()
                        .put("page-title", "Account Verification")
                        .put("title", "Verify E-mail Address")
                        .put("description", "To login your account, you need to verify this e-mail address.")
                        .put("button-text", "Verify My E-Mail Address")
                ),
                LangType.HU to mapOf<String, Any>(
                    "subject" to "",
                    "template-params" to JsonObject()
                ),
            )
        ),
        RESET_PASSWORD(
            TokenUtil.SUBJECT.RESET_PASSWORD,
            "/reset-password?token=%s",
            "view/mail/ResetPasswordMail.hbs",
            mapOf<LangType, Map<String, Any>>(
                LangType.TR to mapOf<String, Any>(
                    "subject" to "Şifre Sıfırla",
                    "template-params" to JsonObject()
                ),
                LangType.EN to mapOf<String, Any>(
                    "subject" to "Reset Password",
                    "template-params" to JsonObject()
                        .put("page-title", "Reset Password")
                        .put("title", "Reset Password")
                        .put("description", "If you didn't do this request, please ignore this e-mail.")
                        .put("button-text", "Reset My Password")
                ),
                LangType.HU to mapOf<String, Any>(
                    "subject" to "",
                    "template-params" to JsonObject()
                )
            )
        )
    }

    enum class LangType {
        TR,
        EN, // EN (US)
        HU
    }

    fun sendMail(
        userID: Int,
        mailType: MailType,
        lang: LangType,
        sqlConnection: SQLConnection,
        templateEngine: HandlebarsTemplateEngine,
        configManager: ConfigManager,
        databaseManager: DatabaseManager,
        mailClient: MailClient,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        TokenUtil.createToken(
            mailType.tokenSubject,
            userID,
            databaseManager,
            sqlConnection
        ) { token, asyncResultOfCreateToken ->
            if (token == null) {
                handler.invoke(null, asyncResultOfCreateToken)

                return@createToken
            }

            databaseManager.getDatabase().userDao.getEmailByID(
                userID,
                sqlConnection
            ) { email, asyncResultOfGetEmailByID ->
                if (email == null) {
                    handler.invoke(null, asyncResultOfGetEmailByID)

                    return@getEmailByID
                }

                val message = MailMessage()

                val activationLink = mailType.tokenAddress.format(token)

                message.from = (configManager.getConfig()["email"] as Map<*, *>)["address"] as String
                message.subject = mailType.templateParametersByLanguage.getValue(lang)["subject"] as String
                message.setTo(email)

                val templateEngineParams =
                    mailType.templateParametersByLanguage.getValue(lang)["template-params"] as JsonObject

                templateEngineParams.put("website-address", configManager.getConfig()["ui-address"] as String)
                templateEngineParams.put("email-address", email)
                templateEngineParams.put("link", activationLink)

                templateEngine.render(
                    templateEngineParams,
                    (configManager.getConfig()["resourcesDir"] as String) + mailType.templatePath
                ) { render ->
                    if (render.failed()) {
                        handler.invoke(null, render)

                        return@render
                    }

                    message.text = render.result().toString()
                    message.html = render.result().toString()

                    mailClient.sendMail(message) { sendMailResult ->
                        if (sendMailResult.failed()) {
                            handler.invoke(null, sendMailResult)

                            return@sendMail
                        }

                        handler.invoke(Successful(), sendMailResult)
                    }
                }
            }
        }
    }
}