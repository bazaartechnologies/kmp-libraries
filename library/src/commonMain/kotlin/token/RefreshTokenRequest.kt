package com.bazaartech.core_network.token

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenRequest(
    @SerialName("username")
    val userName: String,
    @SerialName("token")
    val refreshToken: String
)
