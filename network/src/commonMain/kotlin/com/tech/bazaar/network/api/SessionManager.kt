package com.tech.bazaar.network.api

import kotlinx.coroutines.flow.Flow

interface SessionManager {
    fun observeTokenState(): Flow<TokenState>

    suspend fun getAuthToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun getUsername(): String?

    suspend fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String)

    suspend fun onTokenExpires()
}

sealed interface TokenState {
    data object Idle: TokenState
    data object NewTokenAvailable: TokenState
}