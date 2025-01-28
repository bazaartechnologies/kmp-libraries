package com.tech.bazaar.network.token

import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.ResultState
import com.tech.bazaar.network.common.CLIENT_KEY
import com.tech.bazaar.network.common.CUSTOMER_APP_KEY


internal interface TokenRefreshService {
    suspend fun renewAccessToken(
        username: String,
        refreshToken: String
    ): ResultState<RefreshTokenResponse>
}

internal class DefaultTokenRefreshService(private val client: NetworkClient) : TokenRefreshService {
    override suspend fun renewAccessToken(
        username: String,
        refreshToken: String
    ): ResultState<RefreshTokenResponse> {
        val request = RefreshTokenRequest(
            userName = username,
            refreshToken = refreshToken
        )

        return client.post(
            url = "/v3/auth/token/renew",
            headers = mapOf(
                CLIENT_KEY to CUSTOMER_APP_KEY
            ),
            body = request
        )
    }
}