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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.Route
import org.json.JSONObject
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AccessTokenAuthenticator @Inject constructor(
    private val tokenRefreshService: TokenRefreshService,
    private val sessionManager: SessionManager,
    private val eventsHelper: EventsHelper
) : Authenticator {

    private val mutex = Mutex()

    override fun authenticate(route: Route?, response: Response): Request? {
        val authToken = sessionManager.getAuthToken()

        if (!isRequestWithAccessToken(response)) {
            return null
        }

        return runBlocking {
            mutex.withLock {
                val newAuthToken = sessionManager.getAuthToken()

                // Access token is refreshed in another thread.
                if (authToken != newAuthToken) {
                    return@runBlocking newRequestWithAccessToken(response.request, newAuthToken)
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
                            return@runBlocking handleSuccess(tokenResponse, response)
                        }

                        is Result.Error -> {
                            val exception = tokenResponse.exception
                            if (exception is HttpException) {
                                val errorBody = exception.response()?.errorBody()

                                val errorCode = getErrorCode(errorBody)
                                val responseCode = exception.response()?.code() ?: 0

                                if (responseCode == 403 ||
                                    errorCode == REFRESH_TOKEN_EXPIRED_CODE ||
                                    errorCode == USER_SESSION_NOT_FOUND_CODE
                                ) {
                                    eventsHelper.logEvent(
                                        EventsNames.EVENT_REFRESH_TOKEN_NOT_VALID,
                                        eventsHelper.getEventProperties(responseCode, errorCode)
                                    )
                                    sessionManager.onTokenExpires()
                                    return@runBlocking null
                                }
                            }

                            logErrorEvent(exception)
                        }
                        else -> {}
                    }
                }

                eventsHelper.logEvent(EventsNames.EVENT_REFRESHING_AUTH_TOKEN_FAILED)
                return@runBlocking null
            }
        }
    }

    private fun logErrorEvent(throwable: Throwable?) {
        val msg = "${throwable?.javaClass?.simpleName}  ${throwable?.message}"

        eventsHelper.logEvent(
            EventsNames.EVENT_REFRESH_TOKEN_API_IO_FAILURE,
            eventsHelper.getEventProperties(0, 0, msg)
        )
    }

    private fun getErrorCode(errorBody: ResponseBody?): Int {
        return try {
            val rawBody = errorBody?.string()
            JSONObject(rawBody).getInt("code")
        } catch (exception: Exception) {
            0
        }
    }

    private fun handleSuccess(
        tokenResponse: Result<RefreshTokenResponse>,
        response: Response
    ): Request {
        tokenResponse.data?.let {
            sessionManager.onTokenRefreshed(
                it.token,
                it.expiresAt,
                it.refreshToken
            )
        }

        return newRequestWithAccessToken(
            response.request,
            tokenResponse.data?.token ?: EMPTY_STRING
        )
    }

    private fun isRequestWithAccessToken(response: Response): Boolean {
        val header = response.request.header(AUTHORIZATION_HEADER)
        return header != null && header.startsWith(PREFIX_AUTH_TOKEN)
    }

    private fun newRequestWithAccessToken(request: Request, accessToken: String): Request {
        return request.newBuilder()
            .header(AUTHORIZATION_HEADER, PREFIX_AUTH_TOKEN + accessToken)
            .build()
    }

    companion object {
        const val REFRESH_TOKEN_EXPIRED_CODE = 1001
        const val USER_SESSION_NOT_FOUND_CODE = 1002
        const val AUTHORIZATION_HEADER = "Authorization"
    }
}
