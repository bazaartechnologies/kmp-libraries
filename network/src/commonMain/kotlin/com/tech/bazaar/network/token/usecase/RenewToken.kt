package com.tech.bazaar.network.token.usecase

import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.api.exception.FailedToRefreshTokensException
import com.tech.bazaar.network.api.exception.TokenHasExpiredException
import com.tech.bazaar.network.event.EventsNames
import com.tech.bazaar.network.event.NetworkEventLogger
import com.tech.bazaar.network.token.DefaultTokenRefreshService
import io.ktor.client.plugins.auth.providers.BearerTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class RenewToken(
    private val sessionManager: SessionManager,
    private val networkEventLogger: NetworkEventLogger,
    authClient: NetworkClient
) {
    private val mutex = Mutex()
    private val tokenRefreshService = DefaultTokenRefreshService(client = authClient)

    suspend operator fun invoke(): BearerTokens? {
        mutex.withLock {
            return renew()
        }
    }

    private suspend fun renew(): BearerTokens? {
        val username = sessionManager.getUsername()
        val refreshToken = sessionManager.getRefreshToken()

        if (refreshToken == null || username == null) {
            sessionManager.onTokenExpires()
            return null
        }

        return try {
            val tokens = tokenRefreshService.renewTokens(
                username = username,
                refreshToken = refreshToken
            )
            BearerTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken
            )
        } catch (exception: TokenHasExpiredException) {
            networkEventLogger.logExceptionEvent(
                eventName = EventsNames.EVENT_REFRESH_TOKEN_NOT_VALID,
                exception = exception
            )
            sessionManager.onTokenExpires()
            null
        } catch (exception: FailedToRefreshTokensException) {
            networkEventLogger.logExceptionEvent(
                eventName = EventsNames.EVENT_REFRESH_TOKEN_API_IO_FAILURE,
                exception = exception
            )
            networkEventLogger.logEvent(EventsNames.EVENT_REFRESHING_AUTH_TOKEN_FAILED)
            null
        }
    }
}