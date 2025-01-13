package com.tech.bazaar.network.interceptor

import com.tech.bazaar.network.api.NetworkEventLogger
import com.tech.bazaar.network.event.EventsProperties
import com.tech.bazaar.network.http.ClientHttpException
import com.tech.bazaar.network.http.CustomHttpException
import com.tech.bazaar.network.http.HttpErrorCodes.CLIENT_END_RANGE
import com.tech.bazaar.network.http.HttpErrorCodes.CLIENT_START_RANGE
import com.tech.bazaar.network.http.HttpErrorCodes.SERVER_END_RANGE
import com.tech.bazaar.network.http.HttpErrorCodes.SERVER_START_RANGE
import com.tech.bazaar.network.http.ServerHttpException
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.statement.request
import io.ktor.http.HttpStatusCode

class ApiFailureInterceptor(private val eventLogger: NetworkEventLogger) {

    val plugin = createClientPlugin("ApiFailureInterceptor") {
        onResponse { response ->
            val httpUrl = response.request.url.toString()

            if (response.status.value in CLIENT_START_RANGE..CLIENT_END_RANGE) {
                val httpException =
                    ClientHttpException(response)
                logException(httpException, httpUrl, response.status)
            } else if (response.status.value in SERVER_START_RANGE..SERVER_END_RANGE) {
                val httpException =
                    ServerHttpException(response)
                logException(httpException, httpUrl, response.status)
            }
        }
    }

    private fun logException(
        httpException: CustomHttpException,
        httpUrl: String,
        statusCode: HttpStatusCode
    ) {
        eventLogger.logException(
            httpException,
            mapOf(
                EventsProperties.API_URL to httpUrl,
                EventsProperties.HTTP_CODE to statusCode.value
            )
        )
    }
}