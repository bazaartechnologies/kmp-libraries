package com.tech.bazaar.kmp.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenRequest(
    @SerialName("username")
    val userName: String,
    @SerialName("token")
    val refreshToken: String
)
