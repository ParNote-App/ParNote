package com.parnote.db

import javax.inject.Inject

abstract class DaoImpl {
    @Inject
    lateinit var databaseManager: DatabaseManager

    abstract val tableName: String
}