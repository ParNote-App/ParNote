package com.parnote.db

import com.parnote.db.model.SchemeVersion
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

abstract class DatabaseMigration(val databaseManager: DatabaseManager) {
    abstract val handlers: List<(
        sqlConnection: SQLConnection,
        tablePrefix: String,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) -> SQLConnection>

    abstract val FROM_SCHEME_VERSION: Int
    abstract val SCHEME_VERSION: Int
    abstract val SCHEME_VERSION_INFO: String

    fun isMigratable(version: Int) = version == FROM_SCHEME_VERSION

    fun migrate(
        sqlConnection: SQLConnection,
        tablePrefix: String
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> Unit = { handler ->
        var currentIndex = 0

        fun invoke() {
            val localHandler: (AsyncResult<*>) -> Unit = {
                when {
                    it.failed() -> handler.invoke(it)
                    currentIndex == handlers.lastIndex -> handler.invoke(it)
                    else -> {
                        currentIndex++

                        invoke()
                    }
                }
            }

            if (currentIndex <= handlers.lastIndex)
                handlers[currentIndex].invoke(sqlConnection, tablePrefix, localHandler)
        }

        invoke()
    }

    fun updateSchemeVersion(
        sqlConnection: SQLConnection
    ): (handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection = { handler ->
        databaseManager.getDatabase().schemeVersionDao.add(
            sqlConnection,
            SchemeVersion(SCHEME_VERSION.toString(), SCHEME_VERSION_INFO)
        ) { _, asyncResult ->
            handler.invoke(asyncResult)
        }
    }
}
