package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_7_8 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 7
    override val SCHEME_VERSION = 8
    override val SCHEME_VERSION_INFO = "Add share link table"

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            createShareLinkTable()
        )

    private fun createShareLinkTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + "share_link"}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `note_id` int(11) NOT NULL UNIQUE,
              `token` text NOT NULL UNIQUE,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Shared links table';
        """
            ) {
                handler.invoke(it)
            }
        }
}