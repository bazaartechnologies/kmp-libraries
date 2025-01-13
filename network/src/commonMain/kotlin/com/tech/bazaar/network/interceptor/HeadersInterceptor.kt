package com.tech.bazaar.network.interceptor

import com.tech.bazaar.network.api.AppVersionDetailsProvider
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION_CODE
import com.tech.bazaar.network.interceptor.Plugin.APP_VERSION_NAME
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.util.AttributeKey

class HeadersPlugin(
    private val versionDetailsProvider: AppVersionDetailsProvider
) : HttpClientPlugin<Unit, HeadersPlugin> {
    override val key: AttributeKey<HeadersPlugin>
        get() = AttributeKey("HeadersPlugin")

    override fun prepare(block: Unit.() -> Unit): HeadersPlugin {
        return HeadersPlugin( versionDetailsProvider)
    }

    override fun install(plugin: HeadersPlugin, scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.State) {
            context.headers {
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
    const val APP_VERSION = "AppVersion"
    const val APP_VERSION_NAME = "AppVersionName"
    const val APP_VERSION_CODE = "AppVersionCode"
}




