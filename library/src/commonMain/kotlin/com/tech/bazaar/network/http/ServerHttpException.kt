package com.tech.bazaar.network.http

import io.ktor.client.statement.HttpResponse

class ServerHttpException(private val response: HttpResponse) : CustomHttpException(
    response
)
