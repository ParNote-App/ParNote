package com.parnote.di.module

import com.parnote.db.DatabaseManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class DatabaseManagerModule(private val mDatabaseManager: DatabaseManager) {

    @Provides
    @Singleton
    fun provideDatabaseManager() = mDatabaseManager
}