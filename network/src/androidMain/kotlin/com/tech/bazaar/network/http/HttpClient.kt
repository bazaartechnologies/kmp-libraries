package com.tech.bazaar.network.http

import android.content.Context
import com.appmattus.certificatetransparency.certificateTransparencyInterceptor
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.PlatformContext
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.Dispatchers
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import javax.net.ssl.SSLPeerUnverifiedException

internal actual fun createHttpClient(
    config: NetworkClientBuilder.ClientConfig,
    context: PlatformContext?,
    configure: HttpClientConfig<*>.() -> Unit
): HttpClient {
    val androidContext = context?.context as? Context
    return HttpClient(OkHttp) {
        engine {
            config {
                if (config.isSslPinningEnabled) {
                    addNetworkInterceptor(
                        interceptor = provideCertificateInterceptor(
                            urls = setOf(
                                config.apiHost
                            )
                        )
                    )
                    if (config.certificatePins.isNotEmpty()){
                        certificatePinner(
                            CertificatePinner.Builder().add(
                                config.apiHost,
                                *config.certificatePins.toTypedArray()
                            ).build()
                        )

                        addInterceptor { chain ->
                            try {
                                chain.proceed(chain.request())
                            } catch (e: SSLPeerUnverifiedException) {
                                println("Server's SSL certificate cannot be trusted")
                                throw SSLPeerUnverifiedException("Cannot trust this server")
                            }
                        }
                    }
                }

                if (config.enableDebugMode && androidContext != null) {

                    addInterceptor(
                        ChuckerInterceptor
                            .Builder(androidContext)
                            .build()
                    )

                    val flipperInterceptor = FlipperInterceptorFactory.createInterceptor(androidContext)
                    flipperInterceptor?.let {
                        addNetworkInterceptor(it)
                    }
                }
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


