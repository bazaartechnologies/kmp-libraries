package com.bazaartech.core_network.http

class ClientHttpException(response: retrofit2.Response<*>) : CustomHttpException(
    response
)
