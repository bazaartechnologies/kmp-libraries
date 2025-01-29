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
            if (config.isSslPinningEnabled) {
                val builder = CertificatePinner.Builder().apply {
                    setOf(config.authHost, config.apiHost).forEach {
                        certificatePins[it]?.let { pin ->
                            add(it, pin)
                        }
                    }
                }

                handleChallenge(builder.build())
            }
            dispatcher = Dispatchers.IO
            pipelining = true
        }
        configure(this)

    }
}

val certificatePins = mapOf(
    "bazaar-api.bazaar.technology" to "sha256/84E3383BE814A25673672C188504FB9F05C4449F599307079C5B102DBE25E118",
    "api.bazaar-pay.com" to "sha256/4A577F1485604F6189052A0CBC80819670BF1AE81CD026B620EBFEB1F8C22375"
)