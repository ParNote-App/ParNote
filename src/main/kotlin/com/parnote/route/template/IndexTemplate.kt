package com.parnote.route.template

import com.parnote.Main
import com.parnote.model.Template
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

class IndexTemplate : Template() {
    private val mHotLinks = mapOf<String, String>()

    override val routes = arrayListOf("/*")

    override val order = 999

    override fun getHandler() = Handler<RoutingContext> { context ->
        val response = context.response()
        val normalisedPath = context.normalisedPath()

        if (!mHotLinks[normalisedPath.toLowerCase()].isNullOrEmpty())
            response.putHeader(
                "location",
                mHotLinks[normalisedPath.toLowerCase()]
            ).setStatusCode(302).end()
        else
            response.sendFile(Main.DEFAULT_PUBLIC_PATH + "index.html").end()
    }
}