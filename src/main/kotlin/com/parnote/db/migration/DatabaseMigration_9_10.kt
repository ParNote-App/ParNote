package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_9_10 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 9
    override val SCHEME_VERSION = 10
    override val SCHEME_VERSION_INFO = "Convert token field to token_id in share_link table."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            renameTokenFieldToTokenID(),
            convertTokenIDFieldToInt(),
            addForeignToShareLinkTable()
        )

    private fun renameTokenFieldToTokenID(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${getTablePrefix()}share_link` RENAME COLUMN `token` TO `token_id`;
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun convertTokenIDFieldToInt(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE `${getTablePrefix()}share_link` MODIFY `token_id` int;
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun addForeignToShareLinkTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE ${getTablePrefix()}share_link
                        ADD CONSTRAINT ${getTablePrefix()}share_link_${getTablePrefix()}token_id_fk
                            FOREIGN KEY (token_id) REFERENCES ${getTablePrefix()}token (id);
        """
            ) {
                handler.invoke(it)
            }
        }
}