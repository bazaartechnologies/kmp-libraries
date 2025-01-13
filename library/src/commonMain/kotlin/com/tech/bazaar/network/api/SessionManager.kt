package com.tech.bazaar.network.api

interface SessionManager {
    fun getAuthToken(): String

    fun getRefreshToken(): String

    fun getUsername(): String

    fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String)

    fun onTokenExpires()

    fun shouldSendWithoutRequest(host: String) = host == "bazaar-api.bazaar.technology"
}
