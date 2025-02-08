package com.tech.bazaar.network.plugin

import com.tech.bazaar.network.api.DefaultInternetConnectivityNotifier
import com.tech.bazaar.network.api.InternetConnectivityNotifier
import com.tech.bazaar.network.api.exception.NoInternetException
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.request.HttpRequestPipeline
import io.ktor.util.AttributeKey

internal class ProceedIfInternetIsConnected private constructor(
    private val connectivity: InternetConnectivityNotifier
) {
    class Config {
        var connectivity: InternetConnectivityNotifier = DefaultInternetConnectivityNotifier.instance
    }

    companion object Plugin : HttpClientPlugin<Config, ProceedIfInternetIsConnected> {
        override val key =
            AttributeKey<ProceedIfInternetIsConnected>("ProceedIfInternetIsConnected")

        override fun prepare(block: Config.() -> Unit): ProceedIfInternetIsConnected {
            val config = Config().apply(block)
            return ProceedIfInternetIsConnected(config.connectivity)
        }

        override fun install(plugin: ProceedIfInternetIsConnected, scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
                if (plugin.connectivity.isDisconnected()) {
                    throw NoInternetException()
                }
                proceed()
            }
        }
    }
}



