package com.parnote.di.module

import com.parnote.model.RouteType
import com.parnote.model.Template
import com.parnote.route.api.*
import com.parnote.route.staticFolder.PublicFolder
import com.parnote.route.template.IndexTemplate
import dagger.Module
import dagger.Provides
import io.vertx.core.Vertx
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.CorsHandler
import io.vertx.ext.web.handler.SessionHandler
import io.vertx.ext.web.sstore.LocalSessionStore
import javax.inject.Singleton

@Module
class RouterModule(private val mVertx: Vertx) {
    @Singleton
    private val mRouter by lazy {
        val router = Router.router(mVertx)

        init(router)

        router
    }

    private val mStaticFolderRouteList by lazy {
        arrayOf(
            PublicFolder()
        )
    }

    private val mTemplateRouteList by lazy {
        arrayOf<Template>(
            IndexTemplate()
        )
    }

    private val mAPIRouteList by lazy {
        arrayOf(
            LoginAPI(),
            ForgotPasswordAPI(),
            RegisterAPI(),
            LogoutAPI(),
            ResetPasswordTokenAPI(),
            ResetPasswordAPI(),
            EmailVerificationAPI(),
            CheckLoggedInAPI(),
            InitialLoggedInAPI(),
            GetUserNotesAPI(),
            AddNoteAPI(),
            EditNoteAPI(),
            MoveNoteTrashAPI(),
            MoveNoteArchiveAPI(),
            DeleteNoteAPI(),
            SettingsChangePasswordAPI(),
            SettingsDeleteAccountAPI(),
            SearchAPI()
        )
    }

    private val mRouteList by lazy {
        listOf(
            *mStaticFolderRouteList,
            *mAPIRouteList,
            *mTemplateRouteList
        )
    }

    private fun init(router: Router) {
        val allowedHeaders: MutableSet<String> = HashSet()
        allowedHeaders.add("x-requested-with")
        allowedHeaders.add("Access-Control-Allow-Origin")
        allowedHeaders.add("origin")
        allowedHeaders.add("Content-Type")
        allowedHeaders.add("accept")
        allowedHeaders.add("X-PINGARUNER")

        val allowedMethods = mutableSetOf<HttpMethod>()
        allowedMethods.add(HttpMethod.GET)
        allowedMethods.add(HttpMethod.POST)
        allowedMethods.add(HttpMethod.OPTIONS)

        allowedMethods.add(HttpMethod.DELETE)
        allowedMethods.add(HttpMethod.PATCH)
        allowedMethods.add(HttpMethod.PUT)

        router.route()
            .handler(BodyHandler.create())
            .handler(
                SessionHandler.create(LocalSessionStore.create(mVertx)).setSessionTimeout(24 * 60 * 60 * 1000)
            ) // 24 hours session timeout
            .handler(
                CorsHandler.create(".*.")
                    .allowCredentials(true)
                    .allowedHeaders(allowedHeaders)
                    .allowedMethods(allowedMethods)
            )

        mRouteList.forEach { route ->
            route.routes.forEach { url ->
                when (route.routeType) {
                    RouteType.ROUTE -> router.route(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.GET -> router.get(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.POST -> router.post(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.DELETE -> router.delete(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                    RouteType.PUT -> router.put(url).order(route.order).handler(route.getHandler())
                        .failureHandler(route.getFailureHandler())
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideRouter() = mRouter!!

}