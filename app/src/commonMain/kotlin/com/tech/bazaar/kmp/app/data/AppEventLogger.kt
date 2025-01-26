package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.network.api.EventLogger

class AppEventLogger: EventLogger {
    override fun logEvent(eventName: String, properties: HashMap<String, Any>) {
        println("logging event $eventName with properties $properties")
    }

    override fun logException(
        exception: Throwable,
        eventName: String,
        properties: Map<String, Any>
    ) {
        println("logging exception $exception with properties $properties and event $eventName")
    }
}