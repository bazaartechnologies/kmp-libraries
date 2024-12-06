package com.tech.bazaar.network.interceptor

import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION_CODE
import com.tech.bazaar.network.utils.AppVersionDetailsProvider
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION_NAME
import com.tech.bazaar.network.interceptor.Plugin.AUTHORIZATION
import com.tech.bazaar.network.interceptor.Plugin.BEARER
import com.tech.bazaar.network.token.AuthTokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.util.AttributeKey
import org.koin.core.component.KoinComponent

class HeadersPlugin(
    val authTokenProvider: AuthTokenProvider,
    val versionDetailsProvider: AppVersionDetailsProvider
) : HttpClientPlugin<Unit, HeadersPlugin>,KoinComponent {
    override val key: AttributeKey<HeadersPlugin>
        get() = AttributeKey("HeadersPlugin")

    override fun prepare(block: Unit.() -> Unit): HeadersPlugin {
        return HeadersPlugin(authTokenProvider, versionDetailsProvider)
    }

    override fun install(plugin: HeadersPlugin, scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.State) {
            context.headers {
                append(AUTHORIZATION, "$BEARER ${authTokenProvider.getAuthToken()}")
                append(APP_VERSION, versionDetailsProvider.getAppVersionCode())
                append(
                    APP_VERSION_NAME,
                    versionDetailsProvider.getAppVersionName()
                )
                append(
                    APP_VERSION_CODE,
                    versionDetailsProvider.getAppVersionCode()
                )
            }
        }
    }

}

object Plugin {
    const val AUTHORIZATION = "Authorization"
    const val APP_VERSION = "AppVersion"
    const val APP_VERSION_NAME = "AppVersionName"
    const val APP_VERSION_CODE = "AppVersionCode"
    const val BEARER = "Bearer"
}




