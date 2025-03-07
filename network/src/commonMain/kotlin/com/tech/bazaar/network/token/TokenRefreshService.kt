package com.tech.bazaar.network.token

import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.ResultState
import com.tech.bazaar.network.api.TokenRefreshService
import com.tech.bazaar.network.api.exception.FailedToRefreshTokensException
import com.tech.bazaar.network.api.exception.HttpApiException
import com.tech.bazaar.network.api.exception.TokenHasExpiredException
import com.tech.bazaar.network.api.model.SessionTokens
import com.tech.bazaar.network.common.CLIENT_KEY
import com.tech.bazaar.network.common.CUSTOMER_APP_KEY

class DefaultTokenRefreshService(private val client: NetworkClient) : TokenRefreshService {
    override suspend fun renewTokens(
        username: String,
        refreshToken: String
    ): SessionTokens {
        val request = RefreshTokenRequest(
            userName = username,
            refreshToken = refreshToken
        )

        val result = client.post<RefreshTokenResponse>(
            url = "/v3/auth/token/renew",
            headers = mapOf(
                CLIENT_KEY to CUSTOMER_APP_KEY
            ),
            body = request
        )

        return when (result) {
            is ResultState.Error -> {
                val exception = result.exception

                if (exception is HttpApiException) {
                    if (exception.httpCode == 403 ||
                        exception.backendCode == REFRESH_TOKEN_EXPIRED_CODE ||
                        exception.backendCode == USER_SESSION_NOT_FOUND_CODE
                    ) {
                        throw TokenHasExpiredException(exception)
                    }
                }

                throw FailedToRefreshTokensException(exception)
            }
            is ResultState.Success -> SessionTokens(
                accessToken = result.data.token,
                refreshToken = result.data.refreshToken,
                expiresAt = result.data.expiresAt
            )
        }
    }

    companion object {
        const val REFRESH_TOKEN_EXPIRED_CODE = "1001"
        const val USER_SESSION_NOT_FOUND_CODE = "1002"
    }
}