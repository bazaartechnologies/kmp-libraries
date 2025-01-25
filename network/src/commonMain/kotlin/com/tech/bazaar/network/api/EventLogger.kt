package com.tech.bazaar.network.api

interface EventLogger {
    fun logEvent(eventName: String, properties: HashMap<String, Any> = HashMap())
    fun logException(
        eventName: String = "",
        exception: Throwable,
        properties: Map<String, Any> = HashMap()
    )
}
