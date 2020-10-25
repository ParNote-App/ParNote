package com.parnote.db

import com.parnote.Main
import javax.inject.Inject

abstract class DaoImpl {
    @Inject
    lateinit var databaseManager: DatabaseManager

    abstract val tableName: String

    init {
        Main.getComponent().inject(this)
    }
}