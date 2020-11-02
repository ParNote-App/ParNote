package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_3_4 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 3
    override val SCHEME_VERSION = 4
    override val SCHEME_VERSION_INFO = "Add permission table"

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createPermissionTable()
        )

    private fun createPermissionTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        databaseManager.getDatabase().permissionDao.init()
}