package com.tech.bazaar.network.http

import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.PlatformContext
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.engine.darwin.certificates.CertificatePinner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

internal actual fun createHttpClient(
    config: NetworkClientBuilder.ClientConfig,
    context: PlatformContext?,
    configure: HttpClientConfig<*>.() -> Unit
): HttpClient {
    return HttpClient(Darwin) {
        engine {
            if (config.isSslPinningEnabled && config.certificatePins.isNotEmpty()) {
                val builder = CertificatePinner.Builder().apply {
                    add(
                        config.apiHost,
                        *config.certificatePins.toTypedArray()
                    )
                }

                handleChallenge(builder.build())
            }
            dispatcher = Dispatchers.IO
            pipelining = true
        }
        configure(this)

    }
}