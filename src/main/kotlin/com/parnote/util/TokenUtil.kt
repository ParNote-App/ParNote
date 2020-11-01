package com.parnote.util

import com.parnote.db.DatabaseManager
import com.parnote.db.model.Token
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection
import java.util.*

object TokenUtil {
    enum class SUBJECT {
        LOGIN_SESSION,
        VERIFY_MAIL
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

            val token = Jwts.builder()
                .setSubject(subject.toString())
                .signWith(
                    Keys.hmacShaKeyFor(
                        Base64.getDecoder().decode(
                            secretKey
                        )
                    )
                )
                .compact()

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
}