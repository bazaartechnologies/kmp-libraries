package com.tech.bazaar.network.token

import com.tech.bazaar.network.common.CLIENT_KEY
import com.tech.bazaar.network.common.CUSTOMER_APP_KEY
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
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
            header(
                CLIENT_KEY,
                CUSTOMER_APP_KEY
            )
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        println("tokenResponse: $response")
        return response.body<RefreshTokenResponse>()
    }
}