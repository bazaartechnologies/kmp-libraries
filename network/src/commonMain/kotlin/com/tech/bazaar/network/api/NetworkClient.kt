package com.tech.bazaar.network.api

import com.tech.bazaar.network.api.exception.BadResponseException
import com.tech.bazaar.network.api.exception.HttpApiException
import com.tech.bazaar.network.api.exception.NetworkClientException
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.request.accept
import io.ktor.client.request.get as httpGet
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post as httpPost
import io.ktor.client.request.put as httpPut
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentLength
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class NetworkClient(
    val httpClient: HttpClient
) {
    suspend inline fun <reified T> get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        params: Map<String, String> = emptyMap()
    ): ResultState<T> {
        return safeApiCall {
            httpGet(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                params.forEach { (key, value) ->
                    parameter(key, value)
                }
                accept(ContentType.Application.Json)
            }
        }
    }

    suspend inline fun <reified T> post(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): ResultState<T> {
        return safeApiCall {
            httpPost(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(body)
            }
        }
    }

    suspend inline fun <reified T> put(
        url: String,
        body: Any,
        headers: Map<String, String> = emptyMap()
    ): ResultState<T> {
        return safeApiCall {
            httpPut(url) {
                headers.forEach { (key, value) ->
                    header(key, value)
                }
                contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
                setBody(body)
            }
        }
    }

    suspend inline fun <reified T> safeApiCall(
        crossinline block: suspend HttpClient.() -> HttpResponse
    ): ResultState<T> {
        return try {
            val response = block(httpClient)
            if (response.status.isSuccess()) {
                val data: T = response.body()
                ResultState.Success(data = data)
            } else {
                val errorBody: ErrorResponse = response.parseErrorBody()
                ResultState.Error(
                    exception = HttpApiException(
                        httpCode = response.status.value,
                        backendCode = errorBody.code,
                        throwable = RuntimeException("Received error response"),
                        errorResponse = errorBody
                    )
                )
            }
        } catch (e: ResponseException) {
            val errorBody: ErrorResponse = e.response.parseErrorBody()
            ResultState.Error(
                exception = HttpApiException(
                    httpCode = e.response.status.value,
                    backendCode = errorBody.code,
                    throwable = e,
                    errorResponse = errorBody
                )
            )
        } catch (e: NoTransformationFoundException) {
            ResultState.Error(
                exception = BadResponseException(e)
            )
        } catch (e: Throwable) {
            ResultState.Error(
                exception = NetworkClientException(
                    message = "An unknown error has occurred",
                    cause = e
                )
            )
        }
    }

    /**
     * Clears the bearer token from the [HttpClient] so it can fetch updated tokens
     */
    fun clearBearerTokens() {
        httpClient.authProvider<BearerAuthProvider>()?.clearToken()
    }
}

suspend fun HttpResponse.parseErrorBody(): ErrorResponse {
    return when {
        this.contentLength() == 0L -> ErrorResponse(
            code = "-1",
            message = "Empty error response body."
        )

        else -> try {
            this.body()
        } catch (e: Exception) {
            ErrorResponse(
                code = "-1",
                message = "Error body could not be parsed."
            )
        }
    }
}
