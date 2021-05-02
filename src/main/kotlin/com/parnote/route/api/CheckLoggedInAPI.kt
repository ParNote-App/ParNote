package com.parnote.route.api

import com.parnote.ErrorCode
import com.parnote.Main.Companion.getComponent
import com.parnote.db.DatabaseManager
import com.parnote.model.*
import com.parnote.util.LoginUtil
import io.vertx.ext.web.RoutingContext
import javax.inject.Inject

class CheckLoggedInAPI : Api() {
    override val routes = arrayListOf("/api/auth/checkLoggedIn")

    override val routeType = RouteType.POST

    init {
        getComponent().inject(this)
    }

    @Inject
    lateinit var databaseManager: DatabaseManager

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        LoginUtil.isLoggedIn(databaseManager, context) { isLoggedIn, _ ->
            if (isLoggedIn)
                handler.invoke(Successful())
            else
                handler.invoke(Error(ErrorCode.NOT_LOGGED_IN))
        }
    }
}