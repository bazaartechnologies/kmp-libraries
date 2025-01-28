package com.tech.bazaar.network.api.exception

import com.tech.bazaar.network.api.ErrorResponse

class HttpApiException(
    val httpCode: Int,
    val backendCode: String,
    val throwable: Throwable,
    val errorResponse: ErrorResponse? = null
): NetworkClientException(
    message = "${throwable::class.simpleName}  ${throwable.message}",
    cause = throwable
)