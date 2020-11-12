package com.parnote.route.api

import com.parnote.Main.Companion.getComponent
import com.parnote.db.DatabaseManager
import com.parnote.model.Api
import com.parnote.model.Result
import com.parnote.model.RouteType
import com.parnote.model.Successful
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class LogoutAPI : Api() {
    override val routes = arrayListOf("/api/auth/logout")

    override val routeType = RouteType.POST

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        LoginUtil.logout(databaseManager, context) { _, _ ->
            handler.invoke(Successful())
        }
    }
}