package com.tech.bazaar.network.builder

import com.tech.bazaar.network.api.Constants.IS_MULTIPART
import com.tech.bazaar.network.api.InternetConnectivityNotifier
import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.NetworkClientBuilder.AppConfig
import com.tech.bazaar.network.api.NetworkClientBuilder.ClientConfig
import com.tech.bazaar.network.api.PlatformContext
import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.api.exception.ConstraintViolationException
import com.tech.bazaar.network.event.NetworkEventLogger
import com.tech.bazaar.network.http.createHttpClient
import com.tech.bazaar.network.plugin.AppendHeaders
import com.tech.bazaar.network.plugin.LogApiFailure
import com.tech.bazaar.network.plugin.ProceedIfInternetIsConnected
import com.tech.bazaar.network.token.usecase.RenewToken
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.callid.CallId
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.AttributeKey
import kotlinx.serialization.json.Json

internal fun buildClient(
    json: Json,
    sessionManager: SessionManager?,
    networkEventLogger: NetworkEventLogger,
    clientConfig: ClientConfig,
    appConfig: AppConfig,
    platformContext: PlatformContext?,
    internetConnectivityNotifier: InternetConnectivityNotifier
): NetworkClient {
    if (clientConfig.apiUrl.isEmpty()) {
        throw ConstraintViolationException("API URL must be provided")
    }
    return createHttpClient(config = clientConfig, context = platformContext) {
        expectSuccess = true

        install(CallId)

        install(UserAgent) {
            agent = appConfig.userAgent
        }

        install(HttpRequestRetry) {
            maxRetries = clientConfig.maxFailureRetries
            if (clientConfig.enableExponentialDelayInRetries) {
                exponentialDelay(baseDelayMs = 1000, maxDelayMs = 10000)
            }
        }

        if (clientConfig.enableDebugMode) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL

                // Configure to avoid logging the body for multipart requests
                filter { request ->
                    request.attributes.getOrNull(AttributeKey<Boolean>(IS_MULTIPART)) == false
                }
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

        if (clientConfig.additionalHeadersToAppend.isNotEmpty()) {
            install(AppendHeaders) {
                putAll(clientConfig.additionalHeadersToAppend)
            }
        }

        install(AppendHeaders) {
            put(key = "Platform", value = appConfig.osName)
        }

        defaultRequest {
            url(urlString = clientConfig.apiUrl)
        }

        if (clientConfig.isAuthorizationEnabled && sessionManager != null) {
            install(Auth) {
                val renewToken = RenewToken(
                    sessionManager = sessionManager,
                    networkEventLogger = networkEventLogger
                )

                bearer {
                    loadTokens {
                        sessionManager.getTokens()?.let {
                            BearerTokens(
                                accessToken = it.accessToken,
                                refreshToken = it.refreshToken
                            )
                        }
                    }
                    refreshTokens {
                        renewToken()
                    }

                    sendWithoutRequest { request ->
                        clientConfig.apiHost == request.url.host
                    }
                }
            }
        }
    }.let { NetworkClient(it) }
}