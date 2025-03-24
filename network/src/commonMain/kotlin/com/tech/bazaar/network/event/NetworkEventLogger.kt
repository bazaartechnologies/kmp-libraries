package com.tech.bazaar.network.event

import com.tech.bazaar.network.api.EventLogger
import com.tech.bazaar.network.api.exception.ClientHttpException
import com.tech.bazaar.network.api.exception.HttpApiException
import com.tech.bazaar.network.api.exception.ServerHttpException
import com.tech.bazaar.network.event.EventsProperties.API_URL
import com.tech.bazaar.network.event.EventsProperties.BACKEND_CODE
import com.tech.bazaar.network.event.EventsProperties.ERROR_MESSAGE
import com.tech.bazaar.network.event.EventsProperties.EXCEPTION_NAME
import com.tech.bazaar.network.event.EventsProperties.HTTP_CODE

internal interface NetworkEventLogger {
    fun logEvent(eventName: String, properties: HashMap<String, Any> = HashMap())

    fun logExceptionEvent(
        eventName: String,
        exception: Throwable,
        properties: HashMap<String, Any> = HashMap()
    )
}

internal class DefaultNetworkEventLogger(
    private val eventLogger: EventLogger
) : NetworkEventLogger {

    override fun logEvent(eventName: String, properties: HashMap<String, Any>) {
        eventLogger.logEvent(eventName, properties)
    }

    override fun logExceptionEvent(
        eventName: String,
        exception: Throwable,
        properties: HashMap<String, Any>
    ) {
        properties[EXCEPTION_NAME] = exception::class.simpleName ?: "UnknownException"
        properties[ERROR_MESSAGE] = exception.message.orEmpty()

        when (exception) {
            is HttpApiException -> {
                properties[HTTP_CODE] = exception.httpCode
                properties[BACKEND_CODE] = exception.backendCode
            }
            is ServerHttpException -> {
                properties[API_URL] = exception.url
                properties[HTTP_CODE] = exception.statusCode
            }
            is ClientHttpException -> {
                properties[API_URL] = exception.url
                properties[HTTP_CODE] = exception.statusCode
            }
        }

        eventLogger.logException(
            exception = exception,
            eventName = eventName,
            properties = properties
        )
    }
}

internal object EventsNames {
    const val EVENT_ACCESS_TOKEN_RENEWAL_REQUESTED = "access_token_renewal_requested"
    const val EVENT_ACCESS_TOKEN_RENEWED = "access_token_renewed"
    const val EVENT_REFRESH_TOKEN_NOT_VALID = "refresh_token_not_valid"
    const val EVENT_SESSION_HAS_EXPIRED = "refresh_token_session_has_expired"
    const val EVENT_REFRESH_TOKEN_API_IO_FAILURE = "refresh_token_api_io_failure"
}

internal object EventsProperties {
    private const val PREFIX = "BZ_"
    const val HTTP_CODE = PREFIX + "httpCode"
    const val BACKEND_CODE = PREFIX + "backendCode"
    const val ERROR_MESSAGE = PREFIX + "ErrorMessage"
    const val API_URL = PREFIX + "ApiUrl"
    const val EXCEPTION_NAME = PREFIX + "ExceptionName"
}
