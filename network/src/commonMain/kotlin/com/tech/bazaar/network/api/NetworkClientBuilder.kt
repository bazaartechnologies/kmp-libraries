package com.tech.bazaar.network.api

import com.tech.bazaar.network.interceptor.HeadersPlugin
import com.tech.bazaar.network.token.DefaultTokenRefreshService
import com.tech.bazaar.network.token.usecase.RenewToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json

class NetworkClientBuilder {
    private var sessionManager: SessionManager? = null
    private var versioningProvider: AppVersionDetailsProvider? = null
    private var networkEventLogger: NetworkEventLogger? = null
    private var clientConfig: ClientConfig = ClientConfig()


    fun sessionManager(manager: SessionManager) = apply { sessionManager = manager }

    fun versioningProvider(provider: AppVersionDetailsProvider) =
        apply { versioningProvider = provider }

    fun networkEventLogger(logger: NetworkEventLogger) = apply { networkEventLogger = logger }

    fun clientConfig(config: ClientConfig) = apply { clientConfig = config }

    data class ClientConfig(
        val apiUrl: String = "",
        val authUrl: String = "",
        val isAuthorizationEnabled: Boolean = false,
        val isSslPinningEnabled: Boolean = false

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

    private fun buildClient(): HttpClient {
        if (clientConfig.apiUrl.isEmpty()) {
            throw NetworkClientException("API URL must be provided")
        }
        if (clientConfig.authUrl.isEmpty()) {
            throw NetworkClientException("Auth URL must be provided")
        }
        return build(
            sessionManager!!,
            versioningProvider!!,
            clientConfig
        )
    }

    fun build(): NetworkClient {
        return NetworkClient(buildClient())
    }

    companion object {
        private fun build(
            sessionManager: SessionManager,
            versioningProvider: AppVersionDetailsProvider,
            clientConfig: ClientConfig
        ): HttpClient {
            return createHttpClient(clientConfig) {
                expectSuccess = true
                install(HttpSend) {
                    maxSendCount = 3 // Retry a maximum of 3 times for failed requests
                }

                install(ContentNegotiation) {
                    json(json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    })
                }

                install(HttpTimeout) {
                    requestTimeoutMillis = 70_000
                    connectTimeoutMillis = 70_000
                    socketTimeoutMillis = 70_000
                }

                install(
                    HeadersPlugin(
                        versioningProvider
                    )
                )
                if (clientConfig.isAuthorizationEnabled) {
                    install(Auth) {
                        val tokenRefreshService = DefaultTokenRefreshService(
                            createHttpClientForTokenRefresh(authUrl = clientConfig.authUrl)
                        )
                        val renewToken = RenewToken(sessionManager, tokenRefreshService)

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

        private fun createHttpClientForTokenRefresh(authUrl: String): HttpClient {
            return HttpClient {
                expectSuccess = true
                install(ContentNegotiation) {
                    json(json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    })
                }

                defaultRequest {
                    url(urlString = authUrl)
                }

                engine {
                    dispatcher = Dispatchers.IO // Replace threadsCount
                    pipelining = true
                }
            }
        }

        private fun performCertificateTransparencyVerification(
            request: HttpRequestBuilder,
            logListServiceUrl: String
        ): VerificationResult {
            // Placeholder logic for actual verification
            return VerificationResult.Success
        }
    }
}


sealed class VerificationResult {
    data object Success : VerificationResult()
    data class Failure(val reason: String) : VerificationResult()
}

interface CTLogger {
    fun log(host: String, result: VerificationResult)
}

private object logger : CTLogger {
    override fun log(host: String, result: VerificationResult) {
        if (result is VerificationResult.Failure) {
            println("Certificate transparency check failed for $host with reason: ${result.toString()}")
        }
    }
}

class NetworkClientException(override val message: String) : RuntimeException(message)