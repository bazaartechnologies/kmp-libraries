package com.tech.bazaar.network.api

interface SessionManager {
    suspend fun getAuthToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun getUsername(): String?

    suspend fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String)

    suspend fun onTokenExpires()
}
