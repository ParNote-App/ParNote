package com.parnote.di.module

import com.parnote.config.ConfigManager
import dagger.Module
import dagger.Provides
import de.triology.recaptchav2java.ReCaptcha
import javax.inject.Singleton

@Module
class RecaptchaModule {

    @Provides
    @Singleton
    fun provideRecaptcha(configManager: ConfigManager): ReCaptcha {
        return ReCaptcha(configManager.getConfig()["recaptcha-secret"] as String)
    }
}