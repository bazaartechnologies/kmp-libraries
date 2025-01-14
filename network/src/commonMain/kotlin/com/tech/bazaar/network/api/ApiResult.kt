package com.tech.bazaar.network.api

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val exception: Throwable? = null) : ApiResult<Nothing> {
        val errorMessage: String
            get() = exception?.message.orEmpty()
    }
    data object Loading : ApiResult<Nothing>
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        val response = apiCall.invoke()
        ApiResult.Success(response)
    } catch (throwable: Throwable) {
        ApiResult.Error(throwable)
    }
}