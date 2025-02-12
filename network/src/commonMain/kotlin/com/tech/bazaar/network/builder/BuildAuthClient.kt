package com.tech.bazaar.network.builder

import com.tech.bazaar.network.api.InternetConnectivityNotifier
import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.PlatformContext
import com.tech.bazaar.network.api.exception.ConstraintViolationException
import com.tech.bazaar.network.event.NetworkEventLogger
import com.tech.bazaar.network.http.createHttpClient
import com.tech.bazaar.network.plugin.LogApiFailure
import com.tech.bazaar.network.plugin.ProceedIfInternetIsConnected
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.callid.CallId
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun buildAuthClient(
    json: Json,
    clientConfig: NetworkClientBuilder.ClientConfig,
    platformContext: PlatformContext?,
    networkEventLogger: NetworkEventLogger,
    appConfig: NetworkClientBuilder.AppConfig,
    internetConnectivityNotifier: InternetConnectivityNotifier
): NetworkClient {
    if (clientConfig.authUrl.isEmpty()) {
        throw ConstraintViolationException("Auth URL must be provided")
    }

    return createHttpClient(config = clientConfig, context = platformContext) {
        expectSuccess = true

        install(CallId)

        install(UserAgent) {
            agent = appConfig.userAgent
        }

        if (clientConfig.enableDebugMode) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }

            install(LogApiFailure) {
                eventLogger = networkEventLogger
            }
        }

        if (clientConfig.alwaysCheckInternetConnectivity) {
            install(ProceedIfInternetIsConnected) {
                connectivity = internetConnectivityNotifier
            }
        }

        install(ContentNegotiation) {
            json(json = json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 70_000
            connectTimeoutMillis = 70_000
            socketTimeoutMillis = 70_000
        }

        defaultRequest {
            url(urlString = clientConfig.authUrl)
        }
    }.let { NetworkClient(it) }
}