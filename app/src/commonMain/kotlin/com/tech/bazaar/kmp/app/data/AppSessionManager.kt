package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.kmp.app.data.model.RefreshTokenRequest
import com.tech.bazaar.kmp.app.data.model.RefreshTokenResponse
import com.tech.bazaar.kmp.app.data.repository.SessionStorage
import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.ResultState
import com.tech.bazaar.network.api.SessionManager
import com.tech.bazaar.network.api.exception.FailedToRefreshTokensException
import com.tech.bazaar.network.api.exception.HttpApiException
import com.tech.bazaar.network.api.exception.TokenHasExpiredException
import com.tech.bazaar.network.api.model.SessionTokens
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AppSessionManager(
    private val sessionStorage: SessionStorage,
    private val client: NetworkClient
) : SessionManager {
    private val mutex = Mutex()

    override suspend fun getTokens(): SessionTokens? {
        val refreshToken = getRefreshToken()
        val accessToken = getAuthToken()

        if (refreshToken.isNullOrEmpty() || accessToken.isNullOrEmpty()) {
            return null
        }
        return SessionTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            expiresAt = ""
        )
    }

    override suspend fun renewTokens(): SessionTokens {
        mutex.withLock {
            return renew()
        }
    }

    private suspend fun renew(): SessionTokens {
        val userName = getUsername()
        val refreshToken = getRefreshToken()

        if (userName.isNullOrEmpty() || refreshToken.isNullOrEmpty()) {
            throw TokenHasExpiredException(RuntimeException("Token or username is empty"))
        }

        val request = RefreshTokenRequest(
            userName = userName,
            refreshToken = refreshToken
        )

        val result = client.post<RefreshTokenResponse>(
            url = "/v3/auth/token/renew",
            headers = mapOf(
                CLIENT_KEY to CUSTOMER_APP_KEY
            ),
            body = request
        )

        return when (result) {
            is ResultState.Error -> {
                val exception = result.exception

                if (exception is HttpApiException) {
                    if (exception.httpCode == 403 ||
                        exception.backendCode == REFRESH_TOKEN_EXPIRED_CODE ||
                        exception.backendCode == USER_SESSION_NOT_FOUND_CODE
                    ) {
                        // anything to do on token expiry, like show force logout
                        sessionStorage.clear()
                        throw TokenHasExpiredException(exception)
                    }
                }

                throw FailedToRefreshTokensException(exception)
            }

            is ResultState.Success -> {
                // save on success in data
                sessionStorage.set(SessionStorage.ACCESS_TOKEN, result.data.token)
                sessionStorage.set(SessionStorage.REFRESH_TOKEN, result.data.refreshToken)

                SessionTokens(
                    accessToken = result.data.token,
                    refreshToken = result.data.refreshToken,
                    expiresAt = result.data.expiresAt
                )
            }
        }
    }

    private fun getAuthToken(): String? {
        return sessionStorage.get(SessionStorage.ACCESS_TOKEN)
    }

    private fun getRefreshToken(): String? {
        return sessionStorage.get(SessionStorage.REFRESH_TOKEN)
    }

    private fun getUsername(): String? {
        return sessionStorage.get(SessionStorage.USERNAME)
    }

    companion object {
        const val REFRESH_TOKEN_EXPIRED_CODE = "1001"
        const val USER_SESSION_NOT_FOUND_CODE = "1002"
        const val CLIENT_KEY = "X-Bazaar-Client-Key"
        const val CUSTOMER_APP_KEY = "682521930eaac89fbaeebaeb6ea019f2d8b62489790087135085637897d5e4da"
    }

}