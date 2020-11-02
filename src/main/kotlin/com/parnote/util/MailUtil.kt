package com.parnote.util

import com.parnote.config.ConfigManager
import com.parnote.db.DatabaseManager
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.ext.mail.MailClient
import io.vertx.ext.mail.MailMessage
import io.vertx.ext.sql.SQLConnection

object MailUtil {
    enum class MilType(
        val message: String,
        val messageHTML: String,
        val tokenSubject: TokenUtil.SUBJECT,
        val tokenAddress: String
    ) {
        ACTIVATION(
            "Hello, this is your activation link for your e-mail address: {0}",
            "Hello, this is your activation link for your e-mail address: <a href=\"{0}\">Activate It</a>",
            TokenUtil.SUBJECT.VERIFY_MAIL,
            "http://localhost:8080/activate?token={0}"
        ),
        RESET_PASSWORD(
            "Hello, here is your reset password link: {0}",
            "Hello, here is your reset password link: <a href=\"{0}\">Reset Password</a>",
            TokenUtil.SUBJECT.RESET_PASSWORD,
            "http://localhost:8080/reset-password?token={0}"
        )
    }

    fun sendMail(
        userID: Int,
        mailType: MilType,
        sqlConnection: SQLConnection,
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
                message.subject = "Mail Activation"
                message.setTo(email)

                message.text = mailType.message.format(activationLink)
                message.html = mailType.messageHTML.format(activationLink)

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