package com.tech.bazaar.network.api

import com.tech.bazaar.network.api.model.SessionTokens

fun interface TokenRefreshService {
    suspend fun renewTokens(
        username: String,
        refreshToken: String
    ): SessionTokens
}