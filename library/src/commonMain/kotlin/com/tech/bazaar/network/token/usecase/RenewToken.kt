package com.tech.bazaar.network.token.usecase

import com.tech.bazaar.network.common.REFRESH_TOKEN_EXPIRED_CODE
import com.tech.bazaar.network.common.USER_SESSION_NOT_FOUND_CODE
import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.token.RefreshTokenRequest
import com.tech.bazaar.network.token.TokenRefreshService
import com.tech.bazaar.network.utils.ApiResult
import com.tech.bazaar.network.utils.safeApiCall
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RenewToken(
    private val sessionManager: SessionManager,
    private val tokenRefreshService: TokenRefreshService
) {
    private val mutex = Mutex()

    suspend operator fun invoke(): BearerTokens? {
        mutex.withLock {
            return renew()
        }
    }

    private suspend fun renew(): BearerTokens? {
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
            is ApiResult.Success -> {
                sessionManager.onTokenRefreshed(
                    tokenResponse.data.token,
                    tokenResponse.data.expiresAt,
                    tokenResponse.data.refreshToken
                )

                return BearerTokens(
                    accessToken = tokenResponse.data.token,
                    refreshToken = tokenResponse.data.refreshToken
                )
            }

            is ApiResult.Error -> {
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

        return null
    }
}