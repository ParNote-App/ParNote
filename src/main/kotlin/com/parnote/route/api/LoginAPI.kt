package com.parnote.route.api

import com.parnote.model.Api
import com.parnote.model.Result
import io.vertx.ext.web.RoutingContext

class LoginAPI: Api() {
    override val routes: ArrayList<String> = arrayListOf("/api/testAPI", "/alp/merhaba")

    override fun getHandler(context: RoutingContext, handler: (result: Result) -> Unit) {
        context.response().end("Hello from API.")
    }
}