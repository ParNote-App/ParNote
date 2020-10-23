package com.parnote.di.component

import com.parnote.Main
import com.parnote.di.module.LoggerModule
import com.parnote.di.module.RouterModule
import com.parnote.di.module.VertxModule
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        (VertxModule::class),
        (LoggerModule::class),
        (RouterModule::class),
    ]
)
interface ApplicationComponent {
    fun inject(main: Main)
}