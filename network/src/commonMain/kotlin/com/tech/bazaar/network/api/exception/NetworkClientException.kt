package com.tech.bazaar.network.api.exception

open class NetworkClientException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause)