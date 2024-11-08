package com.bazaartech.core_network.token

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class RefreshTokenRequest(
    @Json(name = "username")
    val userName: String,
    @Json(name = "token")
    val refreshToken: String
)
