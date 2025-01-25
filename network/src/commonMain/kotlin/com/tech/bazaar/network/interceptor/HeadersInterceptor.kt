package com.tech.bazaar.network.interceptor

import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION_CODE
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION_NAME
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.util.AttributeKey

class HeadersPlugin(
    private val appConfig: NetworkClientBuilder.AppConfig
) : HttpClientPlugin<Unit, HeadersPlugin> {
    override val key: AttributeKey<HeadersPlugin>
        get() = AttributeKey("HeadersPlugin")

    override fun prepare(block: Unit.() -> Unit): HeadersPlugin {
        return HeadersPlugin(appConfig = appConfig)
    }

    override fun install(plugin: HeadersPlugin, scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.State) {
            context.headers {
                append(APP_VERSION, appConfig.versionCode)
                append(
                    APP_VERSION_NAME,
                    appConfig.versionName
                )
                append(
                    APP_VERSION_CODE,
                    appConfig.versionCode
                )
            }
        }
    }

}

object Plugin {
    const val APP_VERSION = "AppVersion"
    const val APP_VERSION_NAME = "AppVersionName"
    const val APP_VERSION_CODE = "AppVersionCode"
}




