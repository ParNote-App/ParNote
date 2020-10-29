package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_2_3 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 2
    override val SCHEME_VERSION = 3
    override val SCHEME_VERSION_INFO = "Add Token Table"

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createTokenTable()
        )

    private fun createTokenTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        databaseManager.getDatabase().tokenDao.init()
}