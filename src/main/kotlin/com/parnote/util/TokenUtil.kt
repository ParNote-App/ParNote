package com.parnote.util

import com.parnote.db.DatabaseManager
import com.parnote.db.model.Token
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Encoders
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection
import java.util.*


object TokenUtil {
    enum class SUBJECT {
        LOGIN_SESSION,
        VERIFY_MAIL,
        RESET_PASSWORD
    }

    fun createToken(
        subject: SUBJECT,
        userID: Int,
        databaseManager: DatabaseManager,
        sqlConnection: SQLConnection,
        handler: (token: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().userDao.getSecretKeyByID(
            userID,
            sqlConnection
        ) { secretKey, asyncResultOfSecretKey ->
            if (secretKey == null) {
                handler.invoke(null, asyncResultOfSecretKey)

                return@getSecretKeyByID
            }

            val key = Keys.secretKeyFor(SignatureAlgorithm.HS256)

            val token = Jwts.builder()
                .setSubject(subject.toString())
                .setHeaderParam("key", Encoders.BASE64.encode(key.encoded))
                .signWith(
                    Keys.hmacShaKeyFor(
                        Base64.getDecoder().decode(
                            secretKey
                        )
                    )
                )
                .compact()

            if (subject != SUBJECT.LOGIN_SESSION)
                databaseManager.getDatabase().tokenDao.isAnyTokenExistByUserIDAndSubject(
                    userID,
                    subject.toString(),
                    sqlConnection
                ) { exists, asyncResultOfIsAnyTokenExistsByUserIDAndSubject ->
                    if (exists == null) {
                        handler.invoke(null, asyncResultOfIsAnyTokenExistsByUserIDAndSubject)

                        return@isAnyTokenExistByUserIDAndSubject
                    }

                    if (exists)
                        databaseManager.getDatabase().tokenDao.deleteByUserIDAndSubject(
                            userID,
                            subject.toString(),
                            sqlConnection
                        ) { resultOfDeleteByUserIDAndSubject, asyncResultOfDeleteByUserIDAndSubject ->
                            if (resultOfDeleteByUserIDAndSubject == null) {
                                handler.invoke(null, asyncResultOfDeleteByUserIDAndSubject)

                                return@deleteByUserIDAndSubject
                            }

                            addToken(databaseManager, token, userID, subject, sqlConnection, handler)
                        }
                    else
                        addToken(databaseManager, token, userID, subject, sqlConnection, handler)
                }
            else
                addToken(databaseManager, token, userID, subject, sqlConnection, handler)
        }
    }

    private fun addToken(
        databaseManager: DatabaseManager,
        token: String,
        userID: Int,
        subject: SUBJECT,
        sqlConnection: SQLConnection,
        handler: (token: String?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        databaseManager.getDatabase().tokenDao.add(
            Token(-1, token, userID, subject.toString()),
            sqlConnection
        ) { result, asyncResult ->
            if (result == null) {
                handler.invoke(null, asyncResult)

                return@add
            }

            handler.invoke(token, asyncResult)
        }
    }
}