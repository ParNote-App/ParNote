package com.parnote.model

import com.parnote.Main
import com.parnote.db.DatabaseManager
import com.parnote.util.LoginUtil
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

abstract class LoggedInApi : Api() {
    init {
        @Suppress("LeakingThis")
        Main.getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler() = Handler<RoutingContext> { context ->
        LoginUtil.isLoggedIn(databaseManager, context) { isLoggedIn, _ ->
            if (!isLoggedIn) {
                context.reroute("/")

                return@isLoggedIn
            }

            getHandler(context)
        }
    }
}