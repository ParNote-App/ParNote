package com.parnote.route.api


import com.parnote.ErrorCode
import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.db.model.Token
import com.parnote.model.*
import com.parnote.util.TokenUtil
import de.triology.recaptchav2java.ReCaptcha
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class EmailVerificationAPI : Api() {
    override val routes = arrayListOf("/api/auth/emailVerification")

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

        val token = data.getString("token")
        val reCaptcha = data.getString("recaptcha")

        if (!this.reCaptcha.isValid(reCaptcha)) {
            handler.invoke(Error(ErrorCode.RECAPTCHA_NOT_VALID))

            return
        }

        databaseManager.createConnection { sqlConnection, _ ->
            if (sqlConnection == null) {
                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_7))

                return@createConnection
            }

            databaseManager.getDatabase().tokenDao.isTokenExists(token, sqlConnection) { exists, _ ->
                if (exists == null) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_3))
                    }

                    return@isTokenExists
                }

                if (!exists) {
                    databaseManager.closeConnection(sqlConnection) {
                        handler.invoke(Error(ErrorCode.EMAIL_VERIFICATION_INVALID_TOKEN))
                    }

                    return@isTokenExists
                }

                databaseManager.getDatabase().tokenDao.getUserIDFromToken(token, sqlConnection) { userID, _ ->
                    if (userID == null) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_4))
                        }

                        return@getUserIDFromToken
                    }

                    databaseManager.getDatabase().userDao.makeEmailVerifiedByID(userID, sqlConnection) { result, _ ->
                        if (result == null) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_9))

                            return@makeEmailVerifiedByID
                        }

                        databaseManager.getDatabase().tokenDao.delete(
                            Token(
                                -1,
                                token,
                                -1,
                                TokenUtil.SUBJECT.VERIFY_MAIL.toString()
                            ), sqlConnection
                        ) { resultOfDeleteToken, _ ->
                            databaseManager.closeConnection(sqlConnection) {
                                if (resultOfDeleteToken == null) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_27))

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
}









