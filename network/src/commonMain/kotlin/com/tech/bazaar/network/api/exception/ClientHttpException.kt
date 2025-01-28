package com.tech.bazaar.network.api.exception

class ClientHttpException(val statusCode: Int, val url: String) : NetworkClientException(
    message = "Client HTTP exception occurred. Status Code: $statusCode, URL: $url"
)
