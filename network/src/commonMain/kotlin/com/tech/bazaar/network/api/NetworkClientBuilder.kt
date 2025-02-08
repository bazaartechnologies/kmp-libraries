package com.tech.bazaar.network.api

import com.tech.bazaar.network.api.exception.ConstraintViolationException
import com.tech.bazaar.network.event.DefaultNetworkEventLogger
import com.tech.bazaar.network.event.NetworkEventLogger
import com.tech.bazaar.network.http.createHttpClient
import com.tech.bazaar.network.plugin.AppendHeaders
import com.tech.bazaar.network.plugin.LogApiFailure
import com.tech.bazaar.network.plugin.ProceedIfInternetIsConnected
import com.tech.bazaar.network.token.usecase.RenewToken
import io.ktor.client.HttpClient
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
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class NetworkClientBuilder {
    private var sessionManager: SessionManager? = null
    private var networkEventLogger: NetworkEventLogger? = null
    private var platformContext: PlatformContext? = null
    private var clientConfig: ClientConfig = ClientConfig()
    private var appConfig: AppConfig = AppConfig()

    fun sessionManager(manager: SessionManager) = apply { sessionManager = manager }

    fun platformContext(platformContext: PlatformContext) =
        apply { this.platformContext = platformContext }

    fun eventLogger(logger: EventLogger) =
        apply { networkEventLogger = DefaultNetworkEventLogger(logger) }

    fun clientConfig(config: ClientConfig) = apply { clientConfig = config }

    fun appConfig(config: AppConfig) = apply { appConfig = config }

    data class AppConfig(
        val appName: String = "Client",
        val appVersion: String = "0.0.1",
        val osVersion: String = "Unknown",
        val osName: String = "Unknown",
        val deviceName: String = "Unknown",
        val deviceVersion: String = "Unknown"
    ) {
        val userAgent: String
            get() = "$appName/$appVersion ($osName $osVersion; $deviceName $deviceVersion)"
    }

    data class ClientConfig(
        val apiUrl: String = "",
        val authUrl: String = "",
        val isAuthorizationEnabled: Boolean = false,
        val isSslPinningEnabled: Boolean = true,
        val enableDebugMode: Boolean = false,
        val alwaysCheckInternetConnectivity: Boolean = true,
        val maxFailureRetries: Int = 2,
        val enableExponentialDelayInRetries: Boolean = true,
        val additionalHeadersToAppend: Map<String, String> = emptyMap()
    ) {
        val apiHost = try {
            Url(apiUrl).host
        } catch (e: URLParserException) {
            ""
        }
        val authHost = try {
            Url(authUrl).host
        } catch (e: URLParserException) {
            ""
        }
    }

    fun build(): NetworkClient {
        requireNotNull(sessionManager) { "Session manager is required." }
        requireNotNull(networkEventLogger) { "Event logger is required." }
        check(clientConfig.maxFailureRetries >= 0) { "Max failure retries must be greater than 0" }

        val authClient = if (clientConfig.isAuthorizationEnabled) {
            buildAuthClient(
                clientConfig = clientConfig,
                platformContext = platformContext,
                networkEventLogger = networkEventLogger!!,
                appConfig = appConfig,
                internetConnectivityNotifier = DefaultInternetConnectivityNotifier.instance
            ).let { NetworkClient(it) }
        } else null

        return build(
            authClient = authClient,
            sessionManager = sessionManager!!,
            networkEventLogger = networkEventLogger!!,
            clientConfig = clientConfig,
            appConfig = appConfig,
            platformContext = platformContext,
            internetConnectivityNotifier = DefaultInternetConnectivityNotifier.instance
        ).let { NetworkClient(it) }
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        private fun build(
            authClient: NetworkClient?,
            sessionManager: SessionManager,
            networkEventLogger: NetworkEventLogger,
            clientConfig: ClientConfig,
            appConfig: AppConfig,
            platformContext: PlatformContext?,
            internetConnectivityNotifier: InternetConnectivityNotifier
        ): HttpClient {
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

                defaultRequest {
                    url(urlString = clientConfig.apiUrl)
                }

                if (authClient != null) {
                    install(Auth) {
                        val renewToken = RenewToken(
                            sessionManager = sessionManager,
                            authClient = authClient,
                            networkEventLogger = networkEventLogger
                        )

                        bearer {
                            loadTokens {
                                sessionManager.getRefreshToken()?.let {
                                    BearerTokens(
                                        accessToken = sessionManager.getAuthToken().orEmpty(),
                                        refreshToken = it
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
            }
        }

        private fun buildAuthClient(
            clientConfig: ClientConfig,
            platformContext: PlatformContext?,
            networkEventLogger: NetworkEventLogger,
            appConfig: AppConfig,
            internetConnectivityNotifier: InternetConnectivityNotifier
        ): HttpClient {
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
            }
        }

    }
}