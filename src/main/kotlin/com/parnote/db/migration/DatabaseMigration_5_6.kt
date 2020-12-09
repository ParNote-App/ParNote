package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_5_6 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 5
    override val SCHEME_VERSION = 6
    override val SCHEME_VERSION_INFO = "Add name and surname fields to user table"

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            addNameFieldToUserTable(),
            addSurnameFieldToUserTable()
        )

    private fun addNameFieldToUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${getTablePrefix()}user` 
                    ADD `name` varchar(255) NOT NULL;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun addSurnameFieldToUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${getTablePrefix()}user` 
                    ADD `surname` varchar(255) NOT NULL;
                """
            ) {
                handler.invoke(it)
            }
        }
}