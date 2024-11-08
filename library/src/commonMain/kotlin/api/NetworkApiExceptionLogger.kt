package com.bazaartech.core_network.api

interface NetworkApiExceptionLogger {
    fun logException(throwable: Throwable)

    fun logException(throwable: Throwable, customKeys: Map<String, Any>) {
        // this should log exception along with custom keys
    }
}
