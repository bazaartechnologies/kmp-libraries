package com.tech.bazaar.network.api

import io.ktor.client.HttpClient

interface NetworkExternalAPI {
    fun createServiceOnMainGateway(): HttpClient
    fun createServiceOnSecureGateway(): HttpClient
}
