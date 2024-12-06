package com.tech.bazaar.network.http

import io.ktor.client.statement.HttpResponse

class ClientHttpException(private val response: HttpResponse) : CustomHttpException(
    response
)
