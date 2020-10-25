package com.parnote.di.component

import com.parnote.Main
import com.parnote.db.DaoImpl
import com.parnote.di.module.*
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (VertxModule::class),
        (LoggerModule::class),
        (RouterModule::class),
        (ConfigManagerModule::class),
        (RecaptchaModule::class),
        (DatabaseManagerModule::class)
    ]
)
interface ApplicationComponent {
    fun inject(main: Main)

    fun inject(daoImpl: DaoImpl)
}