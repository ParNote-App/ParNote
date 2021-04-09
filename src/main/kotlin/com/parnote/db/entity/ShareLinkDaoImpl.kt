package com.parnote.db.entity

import com.parnote.db.DaoImpl
import com.parnote.db.dao.ShareLinkDao
import io.vertx.core.AsyncResult
import io.vertx.ext.sql.SQLConnection

class ShareLinkDaoImpl(override val tableName: String = "share_link") : DaoImpl(), ShareLinkDao {
    override fun init(): (sqlConnection: SQLConnection, handler: (asyncResult: AsyncResult<*>) -> Unit) -> SQLConnection =
        { sqlConnection, handler ->
            sqlConnection.query(
                """
            CREATE TABLE IF NOT EXISTS `${getTablePrefix() + tableName}` (
              `id` int NOT NULL AUTO_INCREMENT,
              `note_id` int(11) NOT NULL UNIQUE,
              `token` text NOT NULL UNIQUE,
                constraint ${getTablePrefix()}share_link_${getTablePrefix()}note_id_fk
                    foreign key (note_id) references ${getTablePrefix()}note (id),
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Shared links table';
        """
            ) {
                handler.invoke(it)
            }
        }
}