package com.tech.bazaar.network.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(val exception: Throwable? = null) : ApiResult<Nothing>
    data object Loading : ApiResult<Nothing>
}

internal fun <T> Flow<T>.asApiResult(): Flow<ApiResult<T>> {
    return this
        .map<T, ApiResult<T>> {
            ApiResult.Success(it)
        }
        .onStart { emit(ApiResult.Loading) }
        .catch { emit(ApiResult.Error(it)) }
}

internal suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        val response = apiCall.invoke()
        ApiResult.Success(response)
    } catch (throwable: Throwable) {
        ApiResult.Error(throwable)
    }
}

internal fun <T> ApiResult<T>.successOr(fallback: T): T {
    return (this as? ApiResult.Success<T>)?.data ?: fallback
}

internal fun <T> ApiResult<T>.succeeded(): Boolean {
    return this is ApiResult.Success<T>
}

internal val <T> ApiResult<T>.data: T?
    get() = (this as? ApiResult.Success<T>)?.data

fun <T> T.isEmptyResponse(): Boolean {
    return this != null && this is List<*> && this.isEmpty()
}
