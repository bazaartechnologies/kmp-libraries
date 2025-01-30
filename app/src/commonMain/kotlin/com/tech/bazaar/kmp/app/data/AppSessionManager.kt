package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.api.TokenState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AppSessionManager : SessionManager {
    override fun observeTokenState(): Flow<TokenState> = flowOf(TokenState.Idle)

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