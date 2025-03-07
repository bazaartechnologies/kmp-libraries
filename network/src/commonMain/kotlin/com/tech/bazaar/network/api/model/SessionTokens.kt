package com.tech.bazaar.network.api.model

data class SessionTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: String
)