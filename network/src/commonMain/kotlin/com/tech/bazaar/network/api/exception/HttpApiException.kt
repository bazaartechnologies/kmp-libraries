package com.tech.bazaar.network.api.exception

class HttpApiException(
    val httpCode: Int,
    val backendCode: String,
    private val throwable: Throwable
): NetworkClientException(
    message = "${throwable::class.simpleName}  ${throwable.message}",
    cause = throwable
)