package com.tech.bazaar.network.token.usecase

import com.tech.bazaar.network.api.ResultState
import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.api.exception.HttpApiException
import com.tech.bazaar.network.api.safeApiCall
import com.tech.bazaar.network.common.REFRESH_TOKEN_EXPIRED_CODE
import com.tech.bazaar.network.common.USER_SESSION_NOT_FOUND_CODE
import com.tech.bazaar.network.event.EventsNames
import com.tech.bazaar.network.event.NetworkEventLogger
import com.tech.bazaar.network.token.DefaultTokenRefreshService
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RenewToken(
    private val sessionManager: SessionManager,
    private val networkEventLogger: NetworkEventLogger,
    authClient: HttpClient
) {
    private val mutex = Mutex()
    private val tokenRefreshService = DefaultTokenRefreshService(client = authClient)

    suspend operator fun invoke(): BearerTokens? {
        mutex.withLock {
            return renew()
        }
    }

    private suspend fun renew(): BearerTokens? {
        val username = sessionManager.getUsername()
        val refreshToken = sessionManager.getRefreshToken()

        if (refreshToken == null || username == null) {
            sessionManager.onTokenExpires()
            return null
        }

        val tokenResponse =
            safeApiCall {
                tokenRefreshService.renewAccessToken(
                    username,
                    refreshToken
                )
            }

        return when (tokenResponse) {
            is ResultState.Success -> {
                sessionManager.onTokenRefreshed(
                    tokenResponse.data.token,
                    tokenResponse.data.expiresAt,
                    tokenResponse.data.refreshToken
                )

                BearerTokens(
                    accessToken = tokenResponse.data.token,
                    refreshToken = tokenResponse.data.refreshToken
                )
            }

            is ResultState.Error -> {
                val exception = tokenResponse.exception

                if (exception is ResponseException) {
                    val response = exception.response

                    val statusCode = response.status.value
                    val responseBody = response.body<ErrorBody>()
                    val responseCode = responseBody.code

                    if (statusCode == 403 ||
                        responseCode == REFRESH_TOKEN_EXPIRED_CODE ||
                        responseCode == USER_SESSION_NOT_FOUND_CODE
                    ) {
                        networkEventLogger.logExceptionEvent(
                            eventName = EventsNames.EVENT_REFRESH_TOKEN_NOT_VALID,
                            exception = HttpApiException(
                                httpCode = statusCode,
                                backendCode = responseCode,
                                throwable = exception
                            )
                        )
                        sessionManager.onTokenExpires()
                    }
                }

                networkEventLogger.logExceptionEvent(
                    eventName = EventsNames.EVENT_REFRESH_TOKEN_API_IO_FAILURE,
                    exception = HttpApiException(
                        httpCode = 0,
                        backendCode = 0,
                        throwable = exception ?: RuntimeException("Unknown error occurred")
                    )
                )
                networkEventLogger.logEvent(EventsNames.EVENT_REFRESHING_AUTH_TOKEN_FAILED)
                null
            }
        }
    }

    data class ErrorBody(
        val code: Int = 0
    )
}