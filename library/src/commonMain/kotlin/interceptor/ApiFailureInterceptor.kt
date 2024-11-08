package com.bazaartech.core_network.interceptor

import com.bazaartech.core_network.api.NetworkApiExceptionLogger
import com.bazaartech.core_network.event.EventsProperties
import com.bazaartech.core_network.http.ClientHttpException
import com.bazaartech.core_network.http.CustomHttpException
import com.bazaartech.core_network.http.HttpErrorCodes.CLIENT_END_RANGE
import com.bazaartech.core_network.http.HttpErrorCodes.CLIENT_START_RANGE
import com.bazaartech.core_network.http.HttpErrorCodes.SERVER_END_RANGE
import com.bazaartech.core_network.http.HttpErrorCodes.SERVER_START_RANGE
import com.bazaartech.core_network.http.ServerHttpException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.util.AttributeKey
import io.ktor.utils.io.errors.IOException
import org.koin.core.annotation.Single
import org.koin.java.KoinJavaComponent.inject

@Single
class ApiFailureInterceptor {

    private val exceptionLogger: NetworkApiExceptionLogger by inject(NetworkApiExceptionLogger::class.java)

    val plugin = createClientPlugin("ApiFailureInterceptor") {
        onResponse { response ->
            val httpUrl = response.request.url.toString()

            if (response.status.value in CLIENT_START_RANGE..CLIENT_END_RANGE) {
                val httpException = ClientHttpException(response.status, response.bodyAsText())
                logException(httpException, httpUrl, response.status)
            } else if (response.status.value in SERVER_START_RANGE..SERVER_END_RANGE) {
                val httpException = ServerHttpException(response.status, response.bodyAsText())
                logException(httpException, httpUrl, response.status)
            }
        }

        onException { cause ->
            val httpUrl = it.request.url.toString()
            if (cause is IOException) {
                exceptionLogger.logException(cause, mapOf(EventsProperties.API_URL to httpUrl))
            }
        }
    }

    private fun logException(
        httpException: CustomHttpException,
        httpUrl: String,
        statusCode: HttpStatusCode
    ) {
        exceptionLogger.logException(
            httpException,
            mapOf(
                EventsProperties.API_URL to httpUrl,
                EventsProperties.HTTP_CODE to statusCode.value
            )
        )
    }
}

// Usage in Ktor client setup:
// install(ApiFailureInterceptor().plugin)
