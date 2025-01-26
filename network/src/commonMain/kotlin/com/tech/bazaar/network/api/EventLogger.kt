package com.tech.bazaar.network.api

interface EventLogger {
    fun logEvent(eventName: String, properties: HashMap<String, Any> = HashMap())
    fun logException(
        exception: Throwable,
        eventName: String = "",
        properties: Map<String, Any> = HashMap()
    )
}
