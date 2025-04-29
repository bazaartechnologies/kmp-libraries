package com.tech.bazaar.network.api

import com.tech.bazaar.network.api.model.SessionTokens
import kotlin.time.Duration

interface SessionManager {
    suspend fun getTokens(): SessionTokens?
    suspend fun renewTokens(offset: Duration): SessionTokens
}