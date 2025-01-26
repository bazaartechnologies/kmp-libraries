package com.tech.bazaar.network.plugin

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.util.AttributeKey

internal class AppendHeaders private constructor(
    private val headers: Map<String, String>
) {
    class Config {
        var versionName: String = ""
        var versionCode: String = ""
    }

    companion object Plugin : HttpClientPlugin<Config, AppendHeaders> {
        override val key: AttributeKey<AppendHeaders>
            get() = AttributeKey("AppendHeaders")

        override fun prepare(block: Config.() -> Unit): AppendHeaders {
            val config = Config().apply(block)
            return AppendHeaders(
                headers = mutableMapOf<String, String>().apply {
                    if (config.versionName.isNotEmpty()) {
                        put("AppVersionName", config.versionName)
                    }
                    if (config.versionCode.isNotEmpty()) {
                        put("AppVersion", config.versionCode)
                        put("AppVersionCode", config.versionCode)
                    }
                }
            )
        }

        override fun install(plugin: AppendHeaders, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                context.headers {
                    plugin.headers.forEach {
                        append(it.key, it.value)
                    }
                }
            }
        }
    }
}




