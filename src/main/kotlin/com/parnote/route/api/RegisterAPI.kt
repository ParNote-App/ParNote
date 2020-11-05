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
import io.vertx.ext.mail.MailClient
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

/**
 * double shift yap, RouterModule.kt ye git, mAPIRouteList i bul ve classini icine oradakiler gibi ekle
 * AMACI Apinin calismasini saglamak
 */

class RegisterAPI : Api() {
    override val routes = arrayListOf("/api/auth/registerAPI")

    override val routeType = RouteType.POST //Yapilacak islemin methodunu belirtiyorsun

    init {
        getComponent().inject(this) //modulu inject ediyor. ctrl ile injecte git, classini injectle birnevi bagladik classimizi
    }

    @Inject
    lateinit var databaseManager: DatabaseManager //db manageri cagirdik

    @Inject
    lateinit var reCaptcha: ReCaptcha //ReCaptchayi cagirdik

    @Inject
    lateinit var configManager: ConfigManager

    @Inject
    lateinit var mailClient: MailClient

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {

        val data = context.bodyAsJson //contextin bodysini alip jsona cevirdik

        val name = data.getString("name") //body den sadece string olarak name i aldim
        val surname = data.getString("surname")
        val username = data.getString("username")
        val email = data.getString("email")
        val password = data.getString("password")
        val termsBox = data.getBoolean("termsBox")
        val reCaptcha = data.getString("recaptcha")
        val ipAddress = context.request().remoteAddress().host()

        validateForm(name, surname, username, email, password, termsBox, reCaptcha, handler) {
            databaseManager.createConnection { sqlConnection, _ ->
                if (sqlConnection == null) { //db e erisim olmazsa null doner onu kontrol edip hatamizi veriyoruz
                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_10))
                    return@createConnection
                }

                databaseManager.getDatabase().userDao.isEmailExists(email, sqlConnection) { emailExists, _ ->
                    if (emailExists == null) {
                        //db connectionu kesmek icin !!createConn kesme!!
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_1))
                        }

                        return@isEmailExists
                    }

                    if (emailExists) {
                        databaseManager.closeConnection(sqlConnection) {
                            handler.invoke(Error(ErrorCode.TAKEN_EMAIL_ERROR))
                        }

                        return@isEmailExists
                    }

                    databaseManager.getDatabase().userDao.isUsernameExists(username, sqlConnection) { usernameExists, _ ->
                        if (usernameExists == null) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_3))
                            }

                            return@isUsernameExists
                        }

                        if (usernameExists) {
                            databaseManager.closeConnection(sqlConnection) {
                                handler.invoke(Error(ErrorCode.TAKEN_USERNAME_ERROR))
                            }

                            return@isUsernameExists
                        }

                        // şuraya kadar geldiyse
                        // hiç bir şey de sorun yok
                        // şimdi geldik adamı kaydetmeye

                        RegisterUtil.register(databaseManager, User(-1, username, email, password, ipAddress), sqlConnection) { isEnrolled ->
                            if (isEnrolled == null) {
                                databaseManager.closeConnection(sqlConnection) {
                                    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_4))
                                }

                                return@register
                            }

                            databaseManager.getDatabase().userDao.getUserIDFromUsernameOrEmail(username, sqlConnection) { userID, _ ->
                                if (userID == null) {
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR_5))
                                    }

                                    return@getUserIDFromUsernameOrEmail
                                }

                                MailUtil.sendMail(userID, MailUtil.MailType.ACTIVATION, sqlConnection, configManager, databaseManager, mailClient) { _, _ ->
                                    databaseManager.closeConnection(sqlConnection) {
                                        handler.invoke(Successful())
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /**databaseManager.getDatabase().""buraya validate'e gore .tablo ismi gelecek"" //bunun yerine userDao kullanilacak
    databaseManager.createConnection { connection, asyncResult ->
    databaseManager.getDatabase().userDao.isEmailExists(email, connection) { exists ->
    } //emailin varligini kontrol eder
     */


    /**context.response().end(
    JsonObject(
    mapOf(
    "Result" to "OKAY!"
    )
    ).toJsonString()) //Api cevapliyor ama biz bunu ustteki handler: olmazsa diye yazdik*/

//    handler.invoke(Successful()) // usttekinin aynisini yapiyor bunu tercih et
    /** handler ile isin bittiginde hep bunu kullanmak zorundasin mesela varolan kullanici adi
     * uzerine aynisi olusturulmaya calisildi diyelim db de kontrol edildi ardindan cakisma varsa hata donecek ve handler
     * ile bitiricem !!handler sonda olmak zzorunda degildir*/

//    handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))
    /** Hata mesaj kodu olusturduk ama bunu util->ErrorCode classina belirttik hata kodunu*/


    fun validateForm(name: String, surname: String, username: String, email: String, password: String, termsBox: Boolean,
                     reCaptcha: String, errorHandler: (result: Result) -> Unit, successHandler: () -> Unit) {

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

        if (!email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {//email kosullarimiza uymuyor
            errorHandler.invoke(Error(ErrorCode.REGISTER_EMAIL_INVALID)) //Error donuyoruz
            return //error handleri bitirmek icin
        }

        if (password.isEmpty()) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_PASSWORD_EMPTY))
            return
        }

        if (!password.matches(Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,64}\$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_PASSWORD_INVALID))
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
}