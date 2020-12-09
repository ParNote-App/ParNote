package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.NoteDao
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

class NoteDaoImpl(override val tableName: String = "note") : DaoImpl(), NoteDao {
    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `user_id` int(11) NOT NULL,
              `title` MEDIUMTEXT NOT NULL,
              `text` MEDIUMTEXT NOT NULL,
              `last_modified` MEDIUMTEXT NOT NULL,
              `status` int(1) NOT NULL DEFAULT 0,
              `favorite` int(1) NOT NULL DEFAULT 0,
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Note Table';
        """
            ) {
                handler.invoke(it)
            }
        }
}