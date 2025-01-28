package com.tech.bazaar.network.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ResultState<out T> {
    data class Success<T>(val data: T) : ResultState<T>
    data class Error(
        val exception: Throwable? = null,
        val errorResponse: ErrorResponse? = null
    ) : ResultState<Nothing> {
        val errorMessage: String
            get() = exception?.message.orEmpty()
    }
}

@Serializable
data class ErrorResponse(
    @SerialName("code")
    val code: String = "0",
    @SerialName("message")
    val message: String = "",
    @SerialName("errors")
    val errors: List<String> = emptyList()
)