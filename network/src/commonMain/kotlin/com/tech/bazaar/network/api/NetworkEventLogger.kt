package com.tech.bazaar.network.api

interface NetworkEventLogger {
    fun logEvent(eventName: String, properties: HashMap<String, Any> = HashMap())
    fun logException(throwable: Throwable, customKeys: Map<String, Any> = HashMap())
}
