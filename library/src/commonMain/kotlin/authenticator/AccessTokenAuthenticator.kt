package com.bazaartech.core_network.authenticator

import com.bazaartech.core_network.api.SessionManager
import com.bazaartech.core_network.common.EMPTY_STRING
import com.bazaartech.core_network.common.PREFIX_AUTH_TOKEN
import com.bazaartech.core_network.event.EventsHelper
import com.bazaartech.core_network.event.EventsNames
import com.bazaartech.core_network.token.RefreshTokenRequest
import com.bazaartech.core_network.token.RefreshTokenResponse
import com.bazaartech.core_network.token.TokenRefreshService
import com.bazaartech.core_network.utils.Result
import com.bazaartech.core_network.utils.data
import com.bazaartech.core_network.utils.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.core.annotation.Single


@Single
internal class AccessTokenAuthenticator(
    private val tokenRefreshService: TokenRefreshService,
    private val sessionManager: SessionManager,
    private val eventsHelper: EventsHelper,
    private val httpClient: HttpClient
) {

    private val mutex = Mutex()

    suspend fun authenticate(requestBuilder: HttpRequestBuilder) {
        val authToken = sessionManager.getAuthToken()

        if (!isRequestWithAccessToken(requestBuilder)) {
            return
        }

        mutex.withLock {
            val newAuthToken = sessionManager.getAuthToken()

            // Access token is refreshed in another thread.
            if (authToken != newAuthToken) {
                newRequestWithAccessToken(requestBuilder, newAuthToken)
                return
            }

            // Need to refresh an access token
            val tokenRequest =
                RefreshTokenRequest(
                    sessionManager.getUsername(),
                    sessionManager.getRefreshToken()
                )

            repeat(3) {
                val tokenResponse = safeApiCall {
                    tokenRefreshService.renewAccessToken(tokenRequest)
                }

                when (tokenResponse) {
                    is Result.Success -> {
                        handleSuccess(tokenResponse, requestBuilder)
                        return
                    }

                    is Result.Error -> {
                        val exception = tokenResponse.exception
                        if (exception is ResponseException) {
                            val response = exception.response
                            val errorBody = response.bodyAsText()

                            val errorCode = getErrorCode(errorBody)
                            val responseCode = response.status.value

                            if (responseCode == 403 ||
                                errorCode == REFRESH_TOKEN_EXPIRED_CODE ||
                                errorCode == USER_SESSION_NOT_FOUND_CODE
                            ) {
                                eventsHelper.logEvent(
                                    EventsNames.EVENT_REFRESH_TOKEN_NOT_VALID,
                                    eventsHelper.getEventProperties(responseCode, errorCode)
                                )
                                sessionManager.onTokenExpires()
                                return
                            }
                        }

                        logErrorEvent(exception)
                    }
                    else -> {}
                }
            }

            eventsHelper.logEvent(EventsNames.EVENT_REFRESHING_AUTH_TOKEN_FAILED)
        }
    }

    private fun logErrorEvent(throwable: Throwable?) {
        val msg = "${throwable?.let { it::class.simpleName }} ${throwable?.message}"

        eventsHelper.logEvent(
            EventsNames.EVENT_REFRESH_TOKEN_API_IO_FAILURE,
            eventsHelper.getEventProperties(0, 0, msg)
        )
    }


    private fun getErrorCode(errorBody: String): Int {
        return try {
            val json = Json.parseToJsonElement(errorBody).jsonObject
            json["code"]?.jsonPrimitive?.int ?: 0
        } catch (exception: Exception) {
            0
        }
    }

    private fun handleSuccess(
        tokenResponse: Result<RefreshTokenResponse>,
        requestBuilder: HttpRequestBuilder
    ) {
        tokenResponse.data?.let {
            sessionManager.onTokenRefreshed(
                it.token,
                it.expiresAt,
                it.refreshToken
            )
        }

        newRequestWithAccessToken(
            requestBuilder,
            tokenResponse.data?.token ?: EMPTY_STRING
        )
    }

    private fun isRequestWithAccessToken(requestBuilder: HttpRequestBuilder): Boolean {
        val header = requestBuilder.headers[AUTHORIZATION_HEADER]
        return header != null && header.startsWith(PREFIX_AUTH_TOKEN)
    }

    private fun newRequestWithAccessToken(requestBuilder: HttpRequestBuilder, accessToken: String) {
        requestBuilder.header(AUTHORIZATION_HEADER, PREFIX_AUTH_TOKEN + accessToken)
    }

    companion object {
        const val REFRESH_TOKEN_EXPIRED_CODE = 1001
        const val USER_SESSION_NOT_FOUND_CODE = 1002
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}