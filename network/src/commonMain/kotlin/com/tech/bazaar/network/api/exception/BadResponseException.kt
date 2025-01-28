package com.tech.bazaar.network.api.exception

class BadResponseException(
    throwable: Throwable
): NetworkClientException(
    message = "Received invalid response from the server that is either not parsable or malformed",
    cause = throwable
)