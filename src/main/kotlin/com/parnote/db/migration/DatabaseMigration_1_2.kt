package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_1_2 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 1
    override val SCHEME_VERSION = 2
    override val SCHEME_VERSION_INFO = "Add user Table"

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createUserTable()
        )

    private fun createUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        databaseManager.getDatabase().userDao.init()
}