package com.tech.bazaar.network.api

import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import okhttp3.Interceptor

actual fun createHttpClient(
    config: NetworkClientBuilder.ClientConfig,
    context: PlatformContext?,
    configure: HttpClientConfig<*>.() -> Unit
): HttpClient {
    return HttpClient(OkHttp) {
        engine {
            config {
                addNetworkInterceptor(
                    interceptor = provideCertificateInterceptor(
                        urls = setOf(
                            config.apiHost,
                            config.authHost
                        )
                    )
                )
            }
            dispatcher = Dispatchers.IO // Replace threadsCount
            pipelining = true
        }

        configure(this)
    }
}


internal fun provideCertificateInterceptor(urls: Set<String>): Interceptor {
    return certificateTransparencyInterceptor {
        urls.forEach {
            includeHost(it)
        }
    }
}


