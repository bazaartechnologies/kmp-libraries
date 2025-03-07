package com.tech.bazaar.network.api

import com.tech.bazaar.network.builder.buildClient
import com.tech.bazaar.network.event.DefaultNetworkEventLogger
import com.tech.bazaar.network.event.NetworkEventLogger
import io.ktor.http.URLParserException
import io.ktor.http.Url
import kotlinx.serialization.json.Json

class NetworkClientBuilder {
    private var sessionManager: SessionManager? = null
    private var networkEventLogger: NetworkEventLogger? = null
    private var platformContext: PlatformContext? = null
    private var clientConfig: ClientConfig = ClientConfig()
    private var appConfig: AppConfig = AppConfig()
    private var tokenRefreshService: TokenRefreshService? = null
    private var internetConnectivityNotifier: InternetConnectivityNotifier? = null
    private var json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun sessionManager(manager: SessionManager) = apply { sessionManager = manager }

    fun platformContext(platformContext: PlatformContext) =
        apply { this.platformContext = platformContext }

    fun eventLogger(logger: EventLogger) =
        apply { networkEventLogger = DefaultNetworkEventLogger(logger) }

    fun clientConfig(config: ClientConfig) = apply { clientConfig = config }

    fun appConfig(config: AppConfig) = apply { appConfig = config }

    fun internetConnectivityNotifier(notifier: InternetConnectivityNotifier) =
        apply { internetConnectivityNotifier = notifier }

    fun json(json: Json) = apply { this.json = json }

    fun tokenRefreshService(service: TokenRefreshService) = apply {
        tokenRefreshService = service
    }

    data class AppConfig(
        val appName: String = "Client",
        val appVersion: String = "0.0.1",
        val osVersion: String = "Unknown",
        val osName: String = "Unknown",
        val deviceName: String = "Unknown",
        val deviceVersion: String = "Unknown"
    ) {
        val userAgent: String
            get() = "$appName/$appVersion ($osName $osVersion; $deviceName $deviceVersion)"
    }

    data class ClientConfig(
        val apiUrl: String = "",
        val authUrl: String = "",
        val isAuthorizationEnabled: Boolean = false,
        val isSslPinningEnabled: Boolean = true,
        val enableDebugMode: Boolean = false,
        val alwaysCheckInternetConnectivity: Boolean = true,
        val maxFailureRetries: Int = 2,
        val enableExponentialDelayInRetries: Boolean = true,
        val additionalHeadersToAppend: Map<String, String> = emptyMap()
    ) {
        val apiHost = try {
            Url(apiUrl).host
        } catch (e: URLParserException) {
            ""
        }
        val authHost = try {
            Url(authUrl).host
        } catch (e: URLParserException) {
            ""
        }
    }

    fun build(): NetworkClient {
        requireNotNull(sessionManager) { "Session manager is required." }
        requireNotNull(networkEventLogger) { "Event logger is required." }
        check(clientConfig.maxFailureRetries >= 0) { "Max failure retries must be greater than 0" }

        if (clientConfig.isAuthorizationEnabled) {
            requireNotNull(tokenRefreshService) { "Token refresh service is required." }
        }

        return buildClient(
            json = json,
            tokenRefreshService = tokenRefreshService,
            sessionManager = sessionManager!!,
            networkEventLogger = networkEventLogger!!,
            clientConfig = clientConfig,
            appConfig = appConfig,
            platformContext = platformContext,
            internetConnectivityNotifier = internetConnectivityNotifier!!
        )
    }
}