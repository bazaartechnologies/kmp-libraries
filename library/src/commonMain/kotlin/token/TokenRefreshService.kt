package com.bazaartech.core_network.token

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType


internal interface TokenRefreshService {
    suspend fun renewAccessToken(request: RefreshTokenRequest): RefreshTokenResponse
}

internal class TokenRefreshServiceImpl(private val client: HttpClient) : TokenRefreshService {
    override suspend fun renewAccessToken(request: RefreshTokenRequest): RefreshTokenResponse {
        val response: HttpResponse = client.post("/v3/auth/token/renew") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.body<RefreshTokenResponse>()
    }
}