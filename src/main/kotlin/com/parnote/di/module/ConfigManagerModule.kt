package com.parnote.di.module

import com.parnote.util.ConfigManager
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ConfigManagerModule(private val mConfigManager: ConfigManager) {

    @Provides
    @Singleton
    fun provideConfigManager() = mConfigManager
}