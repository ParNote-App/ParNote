package com.parnote.db

import com.parnote.db.dao.SchemeVersionDao
import com.parnote.db.dao.UserDao
import com.parnote.db.entity.SchemeVersionDaoImpl
import com.parnote.db.entity.UserDaoImpl

data class Database(
    val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl(),
    val userDao: UserDao = UserDaoImpl()
) {
    fun init() = listOf(
        schemeVersionDao.init(),
        userDao.init()
    )
}