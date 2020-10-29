package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.TokenDao
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

class TokenDaoImpl(override val tableName: String = "token") : DaoImpl(), TokenDao {
    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${databaseManager.getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `token` text NOT NULL,
              `created_time` MEDIUMTEXT NOT NULL,
              `user_id` int(11) NOT NULL,
              `subject` varchar(255) NOT NULL,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Token Table';
        """
            ) {
                handler.invoke(it)
            }
        }
}