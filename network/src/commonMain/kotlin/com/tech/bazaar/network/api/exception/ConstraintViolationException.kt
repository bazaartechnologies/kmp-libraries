package com.tech.bazaar.network.api.exception

class ConstraintViolationException(override val message: String): NetworkClientException(message)