package com.tech.bazaar.network.api.exception

class HttpApiException(
    val httpCode: Int,
    val backendCode: Int,
    private val throwable: Throwable
): NetworkClientException("${throwable::class.simpleName}  ${throwable.message}")