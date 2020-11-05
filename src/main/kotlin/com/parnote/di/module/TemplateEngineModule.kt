package com.parnote.di.module

import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.ext.web.templ.handlebars.HandlebarsTemplateEngine
import javax.inject.Singleton

@Module
internal class TemplateEngineModule {

    @Provides
    @Singleton
    fun provideTemplateEngine(vertx: Vertx) = HandlebarsTemplateEngine.create(vertx)!!
}