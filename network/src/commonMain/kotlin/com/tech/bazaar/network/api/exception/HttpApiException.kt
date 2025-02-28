package com.tech.bazaar.network.api.exception

import com.tech.bazaar.network.api.ErrorResponse

class HttpApiException(
    val httpCode: Int,
    val backendCode: String,
    val throwable: Throwable,
    val errorResponse: ErrorResponse? = null
) : NetworkClientException(
    message = buildErrorMessage(errorResponse, throwable),
    cause = throwable
)

private fun buildErrorMessage(errorResponse: ErrorResponse?, throwable: Throwable): String {
    val errorMessage = errorResponse?.let {
        it.message.ifBlank {
            it.errors.firstOrNull()
        }
    }.orEmpty()

    return errorMessage.ifBlank {
        "${throwable::class.simpleName} ${throwable.message}"
    }
}