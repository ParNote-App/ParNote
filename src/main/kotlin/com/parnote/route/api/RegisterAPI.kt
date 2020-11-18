package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main.Companion.getComponent
import com.parnote.config.ConfigManager
import com.parnote.db.DatabaseManager
import com.parnote.db.model.User
import com.parnote.model.*
import com.parnote.util.MailUtil
import com.parnote.util.RegisterUtil
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.core.AsyncResult
import io.vertx.ext.mail.MailClient
import io.vertx.ext.sql.SQLConnection
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import javax.inject.Inject

class RegisterAPI : Api() {
    override val routes = arrayListOf("/api/auth/register")

    override val routeType = RouteType.POST

    init {
        getComponent().inject(this)
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

        val name = data.getString("name")
        val surname = data.getString("surname")
        val username = data.getString("username")
        val email = data.getString("email")
        val password = data.getString("password")
        val termsBox = data.getBoolean("termsBox")
        val reCaptcha = data.getString("recaptcha")

        val ipAddress = context.request().remoteAddress().host()

        validateForm(
            name,
            surname,
            username,
            email,
            password,
            termsBox,
            reCaptcha,
            handler,
            (this::validateFormHandler)(handler, email, username, password, ipAddress)
        )
    }

    private fun validateForm(
        name: String, surname: String, username: String, email: String, password: String, termsBox: Boolean,
        reCaptcha: String, errorHandler: (result: Result) -> Unit, successHandler: () -> Unit
    ) {
        if (name.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_NAME_EMPTY))

            return
        }

        if (name.length < 2) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_NAME_SHORT))

            return
        }

        if (name.length > 32) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_NAME_LONG))

            return
        }

        if (surname.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_SURNAME_EMPTY))

            return
        }

        if (surname.length < 2) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_SURNAME_SHORT))

            return
        }

        if (surname.length > 32) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_SURNAME_LONG))

            return
        }

        if (!name.matches(Regex("^[A-Za-z0-9_-]*$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_NAME_INVALID))

            return
        }

        if (!surname.matches(Regex("^[A-Za-z0-9_-]*$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_SURNAME_INVALID))

            return
        }

        if (username.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_USERNAME_EMPTY))

            return
        }

        if (username.length < 3) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_USERNAME_SHORT))

            return
        }

        if (username.length > 32) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_USERNAME_LONG))

            return
        }

        if (!username.matches(Regex("^[a-zA-Z0-9_]+\$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_USERNAME_INVALID))

            return
        }

        if (email.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_EMAIL_EMPTY))

            return
        }

        if (!email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_EMAIL_INVALID))

            return
        }

        if (password.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_PASSWORD_EMPTY))

            return
        }

        if (!password.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,64}\$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_PASSWORD_INVALID))

            return
        }

        if (!termsBox) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_NOT_ACCEPTED_TERMS))

            return
        }

        if (!this.reCaptcha.isValid(reCaptcha)) {
            errorHandler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))

            return
        }

        successHandler.invoke()
    }

    private fun validateFormHandler(
        handler: (result: Result) -> Unit,
        email: String,
        username: String,
        password: String,
        ipAddress: String
    ) = { ->
        databaseManager.createConnection((this::createConnectionHandler)(handler, email, username, password, ipAddress))
    }

    private fun createConnectionHandler(
        handler: (result: Result) -> Unit,
        email: String,
        username: String,
        password: String,
        ipAddress: String
    ) = handler@{ sqlConnection: SQLConnection?, _: AsyncResult<SQLConnection> ->
        if (sqlConnection == null) {
            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_10))

            return@handler
        }

        databaseManager.getDatabase().userDao.isEmailExists(
            email,
            sqlConnection,
            (this::isEmailExistsHandler)(handler, email, username, password, ipAddress, sqlConnection)
        )
    }

    private fun isEmailExistsHandler(
        handler: (result: Result) -> Unit,
        email: String,
        username: String,
        password: String,
        ipAddress: String,
        sqlConnection: SQLConnection
    ) = handler@{ emailExists: Boolean?, _: AsyncResult<*> ->
        if (emailExists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_1))
            }

            return@handler
        }

        if (emailExists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.TAKEN_EMAIL_ERROR))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.isUsernameExists(
            username,
            sqlConnection,
            (this::isUsernameExistsHandler)(handler, email, username, password, ipAddress, sqlConnection)
        )
    }

    private fun isUsernameExistsHandler(
        handler: (result: Result) -> Unit,
        email: String,
        username: String,
        password: String,
        ipAddress: String,
        sqlConnection: SQLConnection
    ) = handler@{ usernameExists: Boolean?, _: AsyncResult<*> ->
        if (usernameExists == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_13))
            }

            return@handler
        }

        if (usernameExists) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.TAKEN_USERNAME_ERROR))
            }

            return@handler
        }

        RegisterUtil.register(
            databaseManager,
            User(-1, username, email, password, ipAddress),
            sqlConnection,
            (this::registerHandler)(handler, username, sqlConnection)
        )
    }

    private fun registerHandler(
        handler: (result: Result) -> Unit,
        username: String,
        sqlConnection: SQLConnection
    ) = handler@{ isEnrolled: Result? ->
        if (isEnrolled == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_15))
            }

            return@handler
        }

        databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(
            username,
            sqlConnection,
            (this::getUserIDFromUsernameOrEmailHandler)(handler, sqlConnection)
        )
    }

    private fun getUserIDFromUsernameOrEmailHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SQLConnection
    ) = handler@{ userID: Int?, _: AsyncResult<*> ->
        if (userID == null) {
            databaseManager.closeConnection(sqlConnection) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_17))
            }

            return@handler
        }

        MailUtil.sendMail(
            userID,
            MailUtil.MailType.ACTIVATION,
            MailUtil.LangType.EN_US,
            sqlConnection,
            templateEngine,
            configManager,
            databaseManager,
            mailClient,
            (this::sendMailHandler)(handler, sqlConnection)
        )
    }

    private fun sendMailHandler(
        handler: (result: Result) -> Unit,
        sqlConnection: SQLConnection
    ) = handler@{ _: Result?, _: AsyncResult<*> ->
        databaseManager.closeConnection(sqlConnection) {
            handler.invoke(Successful())
        }
    }
}