package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.kmp.app.data.repository.SessionStorage
import com.tech.bazaar.network.api.SessionManager

class AppSessionManager(private val sessionStorage: SessionStorage) : SessionManager {
    override suspend fun getAuthToken(): String? {
        return sessionStorage.get(SessionStorage.ACCESS_TOKEN)
    }

    override suspend fun getRefreshToken(): String? {
        return sessionStorage.get(SessionStorage.REFRESH_TOKEN)
    }

    override suspend fun getUsername(): String? {
        return sessionStorage.get(SessionStorage.USERNAME)
    }

    override suspend fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String) {
        sessionStorage.set(SessionStorage.ACCESS_TOKEN, token)
        sessionStorage.set(SessionStorage.REFRESH_TOKEN, refreshToken)
    }

    override suspend fun onTokenExpires() {
        sessionStorage.clear()
    }

}