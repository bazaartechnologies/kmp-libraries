package com.bazaartech.core_network.token

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RefreshTokenResponse(
    @Json(name = "token")
    val token: String,
    @Json(name = "refreshToken")
    val refreshToken: String,
    @Json(name = "expiresAt")
    val expiresAt: String = ""
)
