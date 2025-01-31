package com.tech.bazaar.network.plugin

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.client.request.headers
import io.ktor.util.AttributeKey

internal class AppendHeaders private constructor(
    private val headers: Map<String, String>
) {
    companion object Plugin : HttpClientPlugin<MutableMap<String, String>, AppendHeaders> {
        override val key: AttributeKey<AppendHeaders>
            get() = AttributeKey("AppendHeaders")

        override fun prepare(block: MutableMap<String, String>.() -> Unit): AppendHeaders {
            val config = mutableMapOf<String, String>().apply(block)
            return AppendHeaders(
                headers = config
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




