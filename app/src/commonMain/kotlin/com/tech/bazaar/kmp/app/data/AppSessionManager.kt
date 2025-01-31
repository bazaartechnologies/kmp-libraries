package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.network.api.SessionManager

class AppSessionManager : SessionManager {
    override suspend fun getAuthToken(): String? {
        return ""
    }

    override suspend fun getRefreshToken(): String? {
        return ""
    }

    override suspend fun getUsername(): String? {
        return "03312784998"
    }

    override suspend fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String) {

    }

    override suspend fun onTokenExpires() {
    }

}