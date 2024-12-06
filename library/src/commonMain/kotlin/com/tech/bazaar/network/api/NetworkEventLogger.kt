package com.tech.bazaar.network.api

interface NetworkEventLogger {
    fun logEvent(eventName: String, properties: HashMap<String, Any> = HashMap())
}
