package com.parnote.db

import com.parnote.db.dao.SchemeVersionDao
import com.parnote.db.entity.SchemeVersionDaoImpl

data class Database(
    val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl()
) {
    fun init() = listOf(
        schemeVersionDao.init()
    )
}