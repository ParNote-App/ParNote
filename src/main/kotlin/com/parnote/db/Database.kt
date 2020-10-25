package com.parnote.db

import com.parnote.db.dao.SchemeVersionDao
import com.parnote.db.entity.SchemeVersionDaoImpl
import io.vertx.ext.sql.SQLConnection

data class Database(val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl()) {
    fun init(
        sqlConnection: SQLConnection
    ) = listOf(
        schemeVersionDao.init(sqlConnection)
    )
}