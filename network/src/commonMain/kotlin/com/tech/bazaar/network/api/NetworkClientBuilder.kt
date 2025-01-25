package com.tech.bazaar.network.api

import com.tech.bazaar.network.event.DefaultNetworkEventLogger
import com.tech.bazaar.network.event.NetworkEventLogger
import com.tech.bazaar.network.interceptor.HeadersPlugin
import com.tech.bazaar.network.token.usecase.RenewToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
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
        val versionName: String = "",
        val versionCode: String = ""
    )

    data class ClientConfig(
        val apiUrl: String = "",
        val authUrl: String = "",
        val isAuthorizationEnabled: Boolean = false,
        val isSslPinningEnabled: Boolean = true,
        val enableDebugMode: Boolean = false
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
        val authClient =
            buildAuthClient(clientConfig = clientConfig, platformContext = platformContext)

        requireNotNull(sessionManager) { "Session manager is required." }
        requireNotNull(sessionManager) { "Session manager is required." }

        val httpClient = build(
            authClient = authClient,
            sessionManager = sessionManager!!,
            networkEventLogger = networkEventLogger!!,
            clientConfig = clientConfig,
            appConfig = appConfig,
            platformContext = platformContext
        )

        return NetworkClient(httpClient = httpClient)
    }

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        private fun build(
            authClient: HttpClient,
            sessionManager: SessionManager,
            networkEventLogger: NetworkEventLogger,
            clientConfig: ClientConfig,
            appConfig: AppConfig,
            platformContext: PlatformContext?
        ): HttpClient {
            if (clientConfig.apiUrl.isEmpty()) {
                throw NetworkClientException("API URL must be provided")
            }

            return createHttpClient(config = clientConfig, context = platformContext) {
                expectSuccess = true
                install(HttpSend) {
                    maxSendCount = 3 // Retry a maximum of 3 times for failed requests
                }

                install(ContentNegotiation) {
                    json(json = json)
                }

                install(HttpTimeout) {
                    requestTimeoutMillis = 70_000
                    connectTimeoutMillis = 70_000
                    socketTimeoutMillis = 70_000
                }

                install(
                    plugin = HeadersPlugin(appConfig = appConfig)
                )

                if (clientConfig.isAuthorizationEnabled) {
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

                defaultRequest {
                    url(clientConfig.apiUrl)
                }

            }
        }

        private fun buildAuthClient(
            clientConfig: ClientConfig,
            platformContext: PlatformContext?
        ): HttpClient {
            if (clientConfig.authUrl.isEmpty()) {
                throw NetworkClientException("Auth URL must be provided")
            }

            return createHttpClient(config = clientConfig, context = platformContext) {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(json = json)
                }

                defaultRequest {
                    url(urlString = clientConfig.authUrl)
                }
            }
        }

    }
}

class NetworkClientException(override val message: String) : RuntimeException(message)