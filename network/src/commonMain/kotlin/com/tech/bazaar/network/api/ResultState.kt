package com.tech.bazaar.network.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface ResultState<out T> {
    data class Success<T>(val data: T, val info: ResultInfo = ResultInfo()) : ResultState<T>
    data class Error(
        val exception: Throwable? = null
    ) : ResultState<Nothing> {
        val errorMessage: String
            get() = exception?.message.orEmpty()
    }
}

@Serializable
data class ErrorResponse(
    @SerialName("code")
    val code: String = "-1",
    @SerialName("message")
    val message: String = "",
    @SerialName("errors")
    val errors: List<String> = emptyList()
)

@Serializable
data class ResultInfo(
    @SerialName("headers")
    val headers: Map<String, List<String>> = emptyMap(),
    @SerialName("statusCode")
    val statusCode: Int = 200
)