package com.tech.bazaar.network.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun createHttpClient(clientConfig:NetworkClientBuilder.ClientConfig, configure: HttpClientConfig<*>.() -> Unit): HttpClient