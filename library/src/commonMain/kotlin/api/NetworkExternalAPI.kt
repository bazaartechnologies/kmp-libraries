package com.bazaartech.core_network.api

interface NetworkExternalAPI {

    fun <T> createServiceOnMainGateway(type: Class<T>): T

    fun <T> createServiceOnSecureGateway(type: Class<T>): T
}
