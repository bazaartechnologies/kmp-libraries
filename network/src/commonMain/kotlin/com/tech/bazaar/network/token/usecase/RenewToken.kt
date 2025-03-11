package com.tech.bazaar.network.token.usecase

import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.api.exception.FailedToRefreshTokensException
import com.tech.bazaar.network.api.exception.TokenHasExpiredException
import com.tech.bazaar.network.api.exception.SessionHasExpiredException
import com.tech.bazaar.network.event.EventsNames
import com.tech.bazaar.network.event.NetworkEventLogger
import io.ktor.client.plugins.auth.providers.BearerTokens

internal class RenewToken(
    private val sessionManager: SessionManager,
    private val networkEventLogger: NetworkEventLogger
) {
    suspend operator fun invoke(): BearerTokens? {
        return renew()
    }

    private suspend fun renew(): BearerTokens? {
        return try {
            val tokens = sessionManager.renewTokens()
            BearerTokens(
                accessToken = tokens.accessToken,
                refreshToken = tokens.refreshToken
            )
        } catch (exception: SessionHasExpiredException) {
            networkEventLogger.logExceptionEvent(
                eventName = EventsNames.EVENT_SESSION_HAS_EXPIRED,
                exception = exception
            )
            null
        } catch (exception: TokenHasExpiredException) {
            networkEventLogger.logExceptionEvent(
                eventName = EventsNames.EVENT_REFRESH_TOKEN_NOT_VALID,
                exception = exception
            )
            null
        } catch (exception: FailedToRefreshTokensException) {
            networkEventLogger.logExceptionEvent(
                eventName = EventsNames.EVENT_REFRESH_TOKEN_API_IO_FAILURE,
                exception = exception
            )
            null
        }
    }
}