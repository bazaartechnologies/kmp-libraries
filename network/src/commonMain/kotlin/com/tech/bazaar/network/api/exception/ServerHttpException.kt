package com.tech.bazaar.network.api.exception

class ServerHttpException(val statusCode: Int, val url: String) : NetworkClientException(
    "Server HTTP exception occurred. Status Code: $statusCode, URL: $url"
)