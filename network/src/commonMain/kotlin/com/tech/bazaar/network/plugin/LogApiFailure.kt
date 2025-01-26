package com.tech.bazaar.network.plugin

import com.tech.bazaar.network.api.exception.ClientHttpException
import com.tech.bazaar.network.api.exception.ServerHttpException
import com.tech.bazaar.network.event.NetworkEventLogger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpClientPlugin
import io.ktor.client.statement.HttpReceivePipeline
import io.ktor.util.AttributeKey
import kotlinx.coroutines.launch

internal class LogApiFailure private constructor(
    private val eventLogger: NetworkEventLogger?
) {
    class Config {
        var eventLogger: NetworkEventLogger? = null
    }

    companion object Plugin : HttpClientPlugin<Config, LogApiFailure> {
        override val key: AttributeKey<LogApiFailure> = AttributeKey("EventHandlingPlugin")

        override fun prepare(block: Config.() -> Unit): LogApiFailure {
            val config = Config().apply(block)
            return LogApiFailure(config.eventLogger)
        }

        override fun install(plugin: LogApiFailure, scope: HttpClient) {
            scope.receivePipeline.intercept(HttpReceivePipeline.After) { response ->
                when (response.status.value) {
                    in 400..499 -> {
                        launch {
                            plugin.eventLogger?.logExceptionEvent(
                                eventName = "client_http_error",
                                exception = ClientHttpException(
                                    response.status.value,
                                    response.call.request.url.toString()
                                )
                            )
                        }
                    }

                    in 500..599 -> {
                        launch {
                            plugin.eventLogger?.logExceptionEvent(
                                eventName = "server_http_error",
                                exception = ServerHttpException(
                                    response.status.value,
                                    response.call.request.url.toString()
                                )
                            )
                        }
                    }
                }

                proceedWith(response)
            }
        }
    }
}