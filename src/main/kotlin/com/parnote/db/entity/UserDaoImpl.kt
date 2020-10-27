package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.UserDao
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

class UserDaoImpl(override val tableName: String = "user") : DaoImpl(), UserDao {

    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
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
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='User Table';
        """
            ) {
                handler.invoke(it)
            }
        }
}