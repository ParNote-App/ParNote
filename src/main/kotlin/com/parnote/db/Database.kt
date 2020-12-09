package com.parnote.db

import com.parnote.db.dao.*
import com.parnote.db.entity.*

data class Database(
    val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl(),
    val userDao: UserDao = UserDaoImpl(),
    val tokenDao: TokenDao = TokenDaoImpl(),
    val permissionDao: PermissionDao = PermissionDaoImpl(),
    val noteDao: NoteDao = NoteDaoImpl()
) {
    fun init() = listOf(
        schemeVersionDao.init(),
        userDao.init(),
        tokenDao.init(),
        permissionDao.init(),
        noteDao.init()
    )
}