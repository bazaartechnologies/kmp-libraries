package com.tech.bazaar.network.di

import com.tech.bazaar.network.common.REFRESH_TOKEN_EXPIRED_CODE
import com.tech.bazaar.network.common.USER_SESSION_NOT_FOUND_CODE
import com.tech.bazaar.network.interceptor.HeadersPlugin
import com.tech.bazaar.network.token.RefreshTokenRequest
import com.tech.bazaar.network.token.TokenRefreshService
import com.tech.bazaar.network.token.TokenRefreshServiceImpl
import com.tech.bazaar.network.utils.Result
import com.tech.bazaar.network.utils.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {

    single {
        HttpClient {
            expectSuccess = true
            install(HttpSend) {
                maxSendCount = 3 // Retry a maximum of 3 times for failed requests
            }

            install(ContentNegotiation) {
                json(json = Json { ignoreUnknownKeys = true })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 70_000
                connectTimeoutMillis = 70_000
                socketTimeoutMillis = 70_000
            }

            install(
                HeadersPlugin(
                    get(),
                    get()
                )
            )

            install(Auth) {
                val sessionManager: SessionManager = get()
                val tokenRefreshService: TokenRefreshService = get()
                bearer {
                    loadTokens {
                        BearerTokens(
                            sessionManager.getAuthToken(),
                            sessionManager.getRefreshToken()
                        )
                    }
                    refreshTokens {
                        val tokenRequest =
                            RefreshTokenRequest(
                                sessionManager.getUsername(),
                                sessionManager.getRefreshToken()
                            )
                        val tokenResponse =
                            safeApiCall {
                                tokenRefreshService.renewAccessToken(tokenRequest)
                            }
                        when (tokenResponse) {
                            is Result.Success -> {
                                println(tokenResponse.data.toString())
                                sessionManager.onTokenRefreshed(
                                    tokenResponse.data.token,
                                    tokenResponse.data.expiresAt,
                                    tokenResponse.data.refreshToken
                                )


//                            handleSuccess(tokenResponse, requestBuilder)
                            }

                            is Result.Error -> {
                                val exception = tokenResponse.exception
                                if (exception is ResponseException) {
                                    val response = exception.response

                                    val responseCode = response.status.value

                                    if (responseCode == 403 ||
                                        responseCode == REFRESH_TOKEN_EXPIRED_CODE ||
                                        responseCode == USER_SESSION_NOT_FOUND_CODE
                                    ) {
                                        sessionManager.onTokenExpires()
                                    }
                                }
                            }

                            else -> {}
                        }


                        return@refreshTokens BearerTokens(
                            sessionManager.getAuthToken(),
                            sessionManager.getRefreshToken()
                        )
                    }

                    sendWithoutRequest { request ->
                        request.url.host == "bazaar-api.bazaar.technology"
                    }
                }
            }

            engine {
                // Enable connection pooling, connection retries, etc.
                threadsCount = 4
                pipelining = true
            }


//            if (BuildConfig.DEBUG) {
//                install(Logging) {
//                    level = LogLevel.BODY
//                    logger = Logger.DEFAULT
//                }
//            } else {
//                install(Logging) {
//                    level = LogLevel.NONE
//                }
//            }

//            install(get(named("CertificateInterceptor"))) // Install custom Certificate Interceptor

        }
    }
//    single(named("CertificateInterceptor")) { provideCertificateInterceptor(get(), get()) }


    single<TokenRefreshService> {
        val httpClient: HttpClient = createHttpClient()
        TokenRefreshServiceImpl(httpClient)
    }


    single(named("MainGateway")) {
        val client: HttpClient = get()
        client.config {
            defaultRequest {
                url(get<com.tech.bazaar.network.api.BaseUrls>().getMainGatewayUrl())
            }
        }
    }
    single(named("SecureGateway")) {
        val client: HttpClient = get()
        client.config {
            defaultRequest {
                url(get<com.tech.bazaar.network.api.BaseUrls>().getSecureGatewayUrl())
            }
        }
    }

    single<HeadersPlugin> {
        HeadersPlugin(get(), get())
    }
}

fun provideCertificateInterceptor(
    certTransparencyFlagProvider: com.tech.bazaar.network.api.CertTransparencyFlagProvider,
    baseUrls: com.tech.bazaar.network.api.BaseUrls
) = createClientPlugin("CertificateInterceptor") {
    if (certTransparencyFlagProvider.isFlagEnable().not()) {
        // No-op interceptor if the certificate transparency flag is disabled
        onRequest { request, _ -> }
    } else {
        onRequest { request, _ ->
            val mainGatewayUrl = baseUrls.getMainGatewayUrl()
            val secureGatewayUrl = baseUrls.getSecureGatewayUrl()

            val logListServiceUrl = "https://www.gstatic.com/ct/log_list/v3/"

            if (request.url.toString() in listOf(mainGatewayUrl, secureGatewayUrl)) {
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
}

fun createHttpClient(): HttpClient {
    return HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(json = Json { ignoreUnknownKeys = true })
        }

        defaultRequest {
            url("https://bazaar-api.bazaar.technology")
        }

        engine {
            threadsCount = 4
            pipelining = true
        }
    }
}

fun performCertificateTransparencyVerification(
    request: HttpRequestBuilder,
    logListServiceUrl: String
): VerificationResult {
    // Placeholder logic for actual verification
    return VerificationResult.Success
}

sealed class VerificationResult {
    object Success : VerificationResult()
    data class Failure(val reason: String) : VerificationResult()
}

interface CTLogger {
    fun log(host: String, result: VerificationResult)
}

object logger : CTLogger {
    override fun log(host: String, result: VerificationResult) {
        if (result is VerificationResult.Failure) {
            println("Certificate transparency check failed for $host with reason: ${result.toString()}")
        }
    }
}