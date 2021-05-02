package com.parnote.db.migration

import com.parnote.db.DatabaseManager
import com.parnote.db.DatabaseMigration
import com.parnote.db.model.Permission
import com.parnote.db.model.SchemeVersion
import com.parnote.model.Result
import com.parnote.model.Successful
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonArray
import io.vertx.ext.sql.SQLConnection

@Suppress("ClassName")
class DatabaseMigration_6_7 : DatabaseMigration() {
    override val FROM_SCHEME_VERSION = 6
    override val SCHEME_VERSION = 7
    override val SCHEME_VERSION_INFO = "Drop all tables and recreate everything."

    override val handlers: List<(sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection> =
        listOf(
            dropNoteTable(),
            dropPermissionTable(),
            dropSchemeVersionTable(),
            dropTokenTable(),
            dropUserTable(),
            initNoteTable(),
            initPermissionTable(),
            initSchemeVersionTable(),
            initTokenTable(),
            initUserTable()
        )

    private fun dropNoteTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}note`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropPermissionTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}permission`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropSchemeVersionTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}scheme_version`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropTokenTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}token`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun dropUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
                    DROP TABLE IF EXISTS `${getTablePrefix()}user`;
                """
            ) {
                handler.invoke(it)
            }
        }

    private fun initNoteTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + "note"}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int(11) NOT NULL,
              `title` MEDIUMTEXT NOT NULL,
              `text` MEDIUMTEXT NOT NULL,
              `last_modified` MEDIUMTEXT NOT NULL,
              `status` int(1) NOT NULL DEFAULT 0,
              `favorite` int(1) NOT NULL DEFAULT 0,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Note Table';
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun initPermissionTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + "permission"}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(16) NOT NULL UNIQUE,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Permission table';
        """
            ) {
                if (it.succeeded())
                    createAdminPermission(sqlConnection) {
                        handler.invoke(it)
                    }
                else
                    handler.invoke(it)
            }
        }

    private fun initSchemeVersionTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + "scheme_version"}` (
              `when` timestamp not null default CURRENT_TIMESTAMP,
              `key` varchar(255) not null,
              `extra` varchar(255),
              PRIMARY KEY (`key`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Database scheme version table.';
        """
            ) {
                if (it.succeeded())
                    add(
                        sqlConnection,
                        SchemeVersion(
                            DatabaseManager.DATABASE_SCHEME_VERSION.toString(),
                            DatabaseManager.DATABASE_SCHEME_VERSION_INFO
                        )
                    ) { _, asyncResultAdd ->
                        handler.invoke(asyncResultAdd)
                    }
                else
                    handler.invoke(it)
            }
        }

    private fun initTokenTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + "token"}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `token` text NOT NULL,
              `created_time` MEDIUMTEXT NOT NULL,
              `user_id` int(11) NOT NULL,
              `subject` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Token Table';
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun initUserTable(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + "user"}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `name` varchar(255) NOT NULL,
              `surname` varchar(255) NOT NULL,
              `username` varchar(16) NOT NULL UNIQUE,
              `email` varchar(255) NOT NULL UNIQUE,
              `password` varchar(255) NOT NULL,
              `permission_id` int(11) NOT NULL,
              `registered_ip` varchar(255) NOT NULL,
              `secret_key` text NOT NULL,
              `public_key` text NOT NULL,
              `register_date` MEDIUMTEXT NOT NULL,
              `email_verified` int(1) NOT NULL DEFAULT 0,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User Table';
        """
            ) {
                handler.invoke(it)
            }
        }

    private fun isTherePermission(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (isTherePermission: Boolean?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.queryWithParams(
            """
            SELECT COUNT(name) FROM `${getTablePrefix() + "permission"}` where name = ?
        """,
            JsonArray().add(permission.name)
        ) {
            if (it.succeeded())
                handler.invoke(it.result().results[0].getInteger(0) != 0, it)
            else
                handler.invoke(null, it)
        }
    }

    private fun add(
        permission: Permission,
        sqlConnection: SQLConnection,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) {
        sqlConnection.updateWithParams(
            """
                INSERT INTO `${getTablePrefix() + "permission"}` (name) VALUES (?)
            """.trimIndent(),
            JsonArray().add(permission.name)
        ) {
            if (it.succeeded())
                handler.invoke(Successful(), it)
            else
                handler.invoke(null, it)
        }
    }

    private fun createAdminPermission(
        sqlConnection: SQLConnection,
        handler: (asyncResult: AsyncResult<*>) -> Unit
    ) {
        isTherePermission(Permission(-1, "admin"), sqlConnection) { isTherePermission, asyncResult ->
            when {
                isTherePermission == null -> handler.invoke(asyncResult)
                isTherePermission -> handler.invoke(asyncResult)
                else -> add(Permission(-1, "admin"), sqlConnection) { _, asyncResultAdd ->
                    handler.invoke(asyncResultAdd)
                }
            }
        }
    }

    private fun add(
        sqlConnection: SQLConnection,
        schemeVersion: SchemeVersion,
        handler: (result: Result?, asyncResult: AsyncResult<*>) -> Unit
    ) = sqlConnection.updateWithParams(
        """
            INSERT INTO `${getTablePrefix() + "scheme_version"}` (`key`, `extra`) VALUES (?, ?)
        """.trimIndent(),
        JsonArray()
            .add(schemeVersion.key)
            .add(schemeVersion.extra)
    ) {
        handler.invoke(if (it.succeeded()) Successful() else null, it)
    }!!
}