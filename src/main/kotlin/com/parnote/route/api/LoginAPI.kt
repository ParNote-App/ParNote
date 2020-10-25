package com.parnote.route.api

import com.parnote.model.Api
import com.parnote.model.RouteType
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext


class LoginAPI: Api() {


    override val routes: ArrayList<String> = arrayListOf("/api/testAPI", "/alp/merhaba")


    override fun getHandler() = Handler<RoutingContext>  {
        it.response().end("Hello from API.")
    }


}