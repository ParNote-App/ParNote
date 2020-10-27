package com.parnote.di.component

import com.parnote.Main
import com.parnote.db.DaoImpl
import com.parnote.db.DatabaseMigration
import com.parnote.di.module.*
import com.parnote.route.api.RegisterAPI
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

    fun inject(databaseMigration: DatabaseMigration)

    fun inject(daoImpl: DaoImpl)

    fun inject(registerAPI: RegisterAPI)
}