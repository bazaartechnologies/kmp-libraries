package com.tech.bazaar.network.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class NetworkClient(
    val httpClient: HttpClient
) {
    suspend inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): ResultState<T> {
        return safeApiCall {
            httpClient.get(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                params.forEach { (key, value) ->
                    parameter(key, value)
                }
            }.body()
        }
    }

    suspend inline fun <reified T> post(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): ResultState<T> {
        return safeApiCall {
            httpClient.post(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                contentType(ContentType.Application.Json)
                setBody(body)
            }.body()
        }
    }
}