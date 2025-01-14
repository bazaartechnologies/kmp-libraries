package com.tech.bazaar.network.api

import com.tech.bazaar.network.interceptor.HeadersPlugin
import com.tech.bazaar.network.token.DefaultTokenRefreshService
import com.tech.bazaar.network.token.usecase.RenewToken
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.serialization.json.Json

class NetworkClientBuilder {
    private var apiUrl: String? = null
    private var authUrl: String? = null
    private var sessionManager: SessionManager? = null
    private var versioningProvider: AppVersionDetailsProvider? = null
    private var networkEventLogger: NetworkEventLogger? = null
    private var clientConfig: ClientConfig = ClientConfig()


    fun apiUrl(url: String) = apply { apiUrl = url }

    fun authUrl(url: String) = apply { authUrl = url }

    fun sessionManager(manager: SessionManager) = apply { sessionManager = manager }

    fun versioningProvider(provider: AppVersionDetailsProvider) = apply { versioningProvider = provider }

    fun networkEventLogger(logger: NetworkEventLogger) = apply { networkEventLogger = logger }

    fun clientConfig(config: ClientConfig) = apply { clientConfig = config }

    data class ClientConfig(
         val isAuthorizationEnabled: Boolean = false
    )

    fun build(): HttpClient {
        return build(
            apiUrl!!,
            authUrl!!,
            sessionManager!!,
            versioningProvider!!,
            clientConfig
        )
    }

    fun buildWrapper(): NetworkClient {
        return NetworkClient(build())
    }

    companion object {
        private fun build(
            apiUrl: String,
            authUrl: String,
            sessionManager: SessionManager,
            versioningProvider: AppVersionDetailsProvider,
            clientConfig: ClientConfig
        ): HttpClient {
            return HttpClient {
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
                if(clientConfig.isAuthorizationEnabled) {
                    install(Auth) {
                        val tokenRefreshService = DefaultTokenRefreshService(
                            createHttpClientForTokenRefresh(authUrl = authUrl)
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
                                sessionManager.shouldSendWithoutRequest(request.url.host)
                            }
                        }
                    }
                }

                engine {
                    dispatcher = Dispatchers.IO // Replace threadsCount
                    pipelining = true
                }

                defaultRequest {
                    url(apiUrl)
                }


            }
        }

        private fun provideCertificateInterceptor(
            baseUrls: List<String>
        ) = createClientPlugin("CertificateInterceptor") {
            onRequest { request, _ ->

                val logListServiceUrl = "https://www.gstatic.com/ct/log_list/v3/"

                if (request.url.toString() in baseUrls) {
                    // Add logic to integrate with CT logListService
                    // Implement CT verification
                    val result = performCertificateTransparencyVerification(request, logListServiceUrl)
                    if (result is VerificationResult.Failure) {
                        // Log the failure here or take necessary actions
                        logger.log(request.url.host, result)
                    }
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