package com.tech.bazaar.network.api.exception

open class NetworkClientException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)