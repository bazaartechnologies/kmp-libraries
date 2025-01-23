package com.tech.bazaar.network.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

expect fun createHttpClient(
    config: NetworkClientBuilder.ClientConfig,
    context: PlatformContext?,
    configure: HttpClientConfig<*>.() -> Unit
): HttpClient