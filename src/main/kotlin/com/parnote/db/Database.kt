package com.parnote.db

import com.parnote.db.dao.PermissionDao
import com.parnote.db.dao.SchemeVersionDao
import com.parnote.db.dao.TokenDao
import com.parnote.db.dao.UserDao
import com.parnote.db.entity.PermissionDaoImpl
import com.parnote.db.entity.SchemeVersionDaoImpl
import com.parnote.db.entity.TokenDaoImpl
import com.parnote.db.entity.UserDaoImpl

data class Database(
    val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl(),
    val userDao: UserDao = UserDaoImpl(),
    val tokenDao: TokenDao = TokenDaoImpl(),
    val permissionDao: PermissionDao = PermissionDaoImpl()
) {
    fun init() = listOf(
        schemeVersionDao.init(),
        userDao.init(),
        tokenDao.init(),
        permissionDao.init()
    )
}