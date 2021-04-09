package com.parnote.db.migration

import com.parnote.db.DatabaseMigration
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_8_9 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 8
    override val SCHEME_VERSION = 9
    override val SCHEME_VERSION_INFO = "Add foreign keys"

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            addForeignToShareLinkTable(),
            addForeignToNoteTable(),
            addForeignToUserTable(),
            addForeignToTokenTable()
        )

    private fun addForeignToShareLinkTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE ${getTablePrefix()}share_link
                        ADD CONSTRAINT ${getTablePrefix()}share_link_${getTablePrefix()}note_id_fk
                            FOREIGN KEY (note_id) REFERENCES ${getTablePrefix()}note (id);
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun addForeignToNoteTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE ${getTablePrefix()}note
                        ADD CONSTRAINT ${getTablePrefix()}note_${getTablePrefix()}user_id_fk
                            FOREIGN KEY (user_id) REFERENCES ${getTablePrefix()}user (id);
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun addForeignToUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE ${getTablePrefix()}user
                        ADD CONSTRAINT ${getTablePrefix()}user_${getTablePrefix()}permission_id_fk
                            FOREIGN KEY (permission_id) REFERENCES ${getTablePrefix()}permission (id);
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun addForeignToTokenTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    ALTER TABLE ${getTablePrefix()}token
                        ADD CONSTRAINT ${getTablePrefix()}token_${getTablePrefix()}user_id_fk
                            FOREIGN KEY (user_id) REFERENCES ${getTablePrefix()}user (id);
        """
            ) {
                handler.invoke(it)
            }
        }
}