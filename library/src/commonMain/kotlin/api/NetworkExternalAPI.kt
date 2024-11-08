package com.bazaartech.core_network.api

import io.ktor.client.HttpClient

interface NetworkExternalAPI {
    fun createServiceOnMainGateway(): HttpClient
    fun createServiceOnSecureGateway(): HttpClient
}
