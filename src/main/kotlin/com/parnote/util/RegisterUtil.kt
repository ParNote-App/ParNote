package com.parnote.util

import com.parnote.db.DatabaseManager
import com.parnote.db.model.User
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.ext.sql.SQLConnection

object RegisterUtil {
    fun register(databaseManager: DatabaseManager, user: User, sqlConnection: SQLConnection, handler: (isSuccessful: Result?) -> Unit) {
        databaseManager.getDatabase().userDao.add(user, sqlConnection) { isSuccessful, _ ->
            if (isSuccessful == null) {
                handler.invoke(null)

                return@add
            }

            handler.invoke(Successful())
        }
    }
}