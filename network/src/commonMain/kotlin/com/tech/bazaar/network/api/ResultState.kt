package com.tech.bazaar.network.api

sealed interface ResultState<out T> {
    data class Success<T>(val data: T) : ResultState<T>
    data class Error(val exception: Throwable? = null) : ResultState<Nothing> {
        val errorMessage: String
            get() = exception?.message.orEmpty()
    }
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ResultState<T> {
    return try {
        val response = apiCall.invoke()
        ResultState.Success(response)
    } catch (throwable: Throwable) {
        ResultState.Error(throwable)
    }
}