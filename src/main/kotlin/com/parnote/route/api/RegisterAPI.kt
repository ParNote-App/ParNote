package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main.Companion.getComponent
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import de.triology.recaptchav2java.ReCaptcha
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

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {

        val data = context.bodyAsJson //contextin bodysini alip jsona cevirdik

        val name = data.getString("name") //body den sadece string olarak name i aldim
        val surname = data.getString("surname")
        val username = data.getString("username")
        val email = data.getString("email")
        val password = data.getString("password")
        val termsBox = data.getBoolean("termsBox")

        validateForm(name, surname, username, email, password, termsBox, handler) {
            /**databaseManager.getDatabase().""buraya validate'e gore .tablo ismi gelecek"" //bunun yerine userDao kullanilacak
            databaseManager.createConnection { connection, asyncResult ->
            databaseManager.getDatabase().userDao.isEmailExists(email, connection) { exists ->
            } //emailin varligini kontrol eder
             */
        }

        /**context.response().end(
        JsonObject(
        mapOf(
        "Result" to "OKAY!"
        )
        ).toJsonString()) //Api cevapliyor ama biz bunu ustteki handler: olmazsa diye yazdik*/

        handler.invoke(Successful()) // usttekinin aynisini yapiyor bunu tercih et
        /** handler ile isin bittiginde hep bunu kullanmak zorundasin mesela varolan kullanici adi
         * uzerine aynisi olusturulmaya calisildi diyelim db de kontrol edildi ardindan cakisma varsa hata donecek ve handler
         * ile bitiricem !!handler sonda olmak zzorunda degildir*/

        handler.invoke(Error(ErrorCode.UNKNOWN_ERROR))
        /** Hata mesaj kodu olusturduk ama bunu util->ErrorCode classina belirttik hata kodunu*/

    }

    fun validateForm(name: String, surname: String, username: String, email: String, password: String, termsBox: Boolean,
                     errorHandler: (result: Result) -> Unit, successHandler: () -> Unit) {

        //if (name)

        //if (surname)

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

        if (!username.matches(Regex("^[a-zA-Z0-9]+\$"))) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_USERNAME_INVALID))
            return
        }

        if (!email.matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$"))) {//email kosullarimiza uymuyor
            errorHandler.invoke(Error(ErrorCode.REGISTER_EMAIL_INVALID)) //Error donuyoruz
            return //error handleri bitirmek icin
        }

//      if (password) md5 e cok takilma

        if (termsBox == false) {
            errorHandler.invoke(Error(ErrorCode.REGISTER_NOT_ACCEPTED_TERMS))
            return
        }

        successHandler.invoke()
    }


}