package com.parnote.db

import com.parnote.db.dao.*
import com.parnote.db.entity.*

data class Database(
    val schemeVersionDao: SchemeVersionDao = SchemeVersionDaoImpl(),
    val permissionDao: PermissionDao = PermissionDaoImpl(),
    val userDao: UserDao = UserDaoImpl(),
    val tokenDao: TokenDao = TokenDaoImpl(),
    val noteDao: NoteDao = NoteDaoImpl(),
    val shareLinkDao: ShareLinkDao = ShareLinkDaoImpl()
) {
    fun init() = listOf(
        schemeVersionDao.init(),
        permissionDao.init(),
        userDao.init(),
        tokenDao.init(),
        noteDao.init(),
        shareLinkDao.init()
    )
}