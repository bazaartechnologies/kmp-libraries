package com.tech.bazaar.network.api

import com.tech.bazaar.network.api.model.SessionTokens

interface SessionManager {
    suspend fun getTokens(): SessionTokens?
    suspend fun renewTokens(): SessionTokens
}