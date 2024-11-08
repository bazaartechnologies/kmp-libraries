package com.bazaartech.core_network.http

class ServerHttpException(response: retrofit2.Response<*>) : CustomHttpException(
    response
)
