package com.bazaartech.core_network.token

import com.bazaartech.core_network.common.CLIENT_KEY_HEADER
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

internal interface TokenRefreshService {

    @Headers(CLIENT_KEY_HEADER)
    @POST("/v3/auth/token/renew")
    suspend fun renewAccessToken(@Body request: RefreshTokenRequest): RefreshTokenResponse
}
