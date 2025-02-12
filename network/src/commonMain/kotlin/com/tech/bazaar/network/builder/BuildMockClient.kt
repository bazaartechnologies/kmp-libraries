package com.tech.bazaar.network.builder

import com.tech.bazaar.network.api.MockClientBuilder
import com.tech.bazaar.network.api.NetworkClient
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.callid.CallId
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.toMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal fun buildMockClient(requests: MockClientBuilder.Requests): NetworkClient {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
        encodeDefaults = true
    }
    val mockEngine = MockEngine { request ->
        val method = request.method
        val path = request.url.fullPath

        val matchedResponse = when (method) {
            HttpMethod.Get -> requests.get.find { it.path == path }
            HttpMethod.Post -> requests.post.find { it.path == path }
            HttpMethod.Put -> requests.put.find { it.path == path }
            else -> null
        }

        try {
            val body = request.body.toByteArray().decodeToString()
            matchedResponse?.validateRequest?.invoke(body, request.headers.toMap())
        } catch (e: Exception) {
            return@MockEngine respondError(
                status = HttpStatusCode.BadRequest,
                content = json.encodeToString(
                    MockError.serializer(),
                    MockError(error = "Mock response not found")
                )
            )
        }

        return@MockEngine matchedResponse?.let {
            respond(
                content = it.response,
                status = HttpStatusCode.fromValue(it.responseCode),
                headers = headersOf(
                    "Content-Type", listOf(ContentType.Application.Json.toString())
                )
            )
        } ?: respondError(
            status = HttpStatusCode.NotFound,
            content = json.encodeToString(
                MockError.serializer(),
                MockError(error = "Mock response not found")
            )
        )
    }

    return HttpClient(mockEngine) {
        install(CallId)

        install(UserAgent) {
            agent = "Mock Client"
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }

        install(ContentNegotiation) {
            json(json = json)
        }

        install(HttpTimeout) {
            requestTimeoutMillis = 70_000
            connectTimeoutMillis = 70_000
            socketTimeoutMillis = 70_000
        }
    }.let { NetworkClient(it) }
}

@Serializable
internal data class MockError(
    private val error: String
)