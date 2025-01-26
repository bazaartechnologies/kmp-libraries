package com.tech.bazaar.network.http

import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.PlatformContext
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

internal expect fun createHttpClient(
    config: NetworkClientBuilder.ClientConfig,
    context: PlatformContext?,
    configure: HttpClientConfig<*>.() -> Unit
): HttpClient