package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.kmp.app.data.Constants.GATEWAY_URL
import com.tech.bazaar.kmp.app.data.Constants.IDENTITY_URL
import com.tech.bazaar.kmp.app.data.repository.SessionStorage
import com.tech.bazaar.kmp.app.domain.DefaultTokenRefreshService
import com.tech.bazaar.network.api.DefaultInternetConnectivityNotifier
import com.tech.bazaar.network.api.InternetConnectivityNotifier
import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.PlatformContext
import com.tech.bazaar.network.api.ResultState
import com.tech.bazaar.network.api.TokenRefreshService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GatewayService(platformContext: PlatformContext) {
    private val sessionStorage: SessionStorage = SessionStorage()
    private val internetConnectivityNotifier: InternetConnectivityNotifier = DefaultInternetConnectivityNotifier.instance
    private val authClient: NetworkClient = NetworkClientBuilder()
        .sessionManager(AppSessionManager(sessionStorage))
        .platformContext(platformContext)
        .eventLogger(AppEventLogger())
        .internetConnectivityNotifier(internetConnectivityNotifier)
        .appConfig(NetworkClientBuilder.AppConfig(appName = "kmp-app", appVersion = "1.1.0"))
        .clientConfig(
            NetworkClientBuilder.ClientConfig(
                isAuthorizationEnabled = false,
                isSslPinningEnabled = true,
                apiUrl = IDENTITY_URL,
                enableDebugMode = true
            )
        )
        .build()

    private val tokenRefreshService: TokenRefreshService = DefaultTokenRefreshService(client = authClient)
    private val client: NetworkClient = NetworkClientBuilder()
        .sessionManager(AppSessionManager(sessionStorage))
        .platformContext(platformContext)
        .eventLogger(AppEventLogger())
        .tokenRefreshService(tokenRefreshService)
        .internetConnectivityNotifier(internetConnectivityNotifier)
        .appConfig(NetworkClientBuilder.AppConfig(appName = "kmp-app", appVersion = "1.1.0"))
        .clientConfig(
            NetworkClientBuilder.ClientConfig(
                isAuthorizationEnabled = true,
                isSslPinningEnabled = true,
                apiUrl = GATEWAY_URL,
                authUrl = IDENTITY_URL,
                enableDebugMode = true
            )
        )
        .build()

    init {
        simulateClearingBearerTokens()
    }

    private fun simulateClearingBearerTokens() {
        CoroutineScope(Dispatchers.IO).launch {
            getGuestUserSession("kmp-app-test-device").let {
                if (it is ResultState.Success) {
                    sessionStorage.set(SessionStorage.USERNAME, "kmp-app-test-device")
                    sessionStorage.set(SessionStorage.ACCESS_TOKEN, it.data.token)
                    sessionStorage.set(SessionStorage.REFRESH_TOKEN, it.data.refreshToken)
                }
            }
            delay(15000)
            client.clearBearerTokens()
        }
    }

    private suspend fun getGuestUserSession(
        deviceId: String
    ): ResultState<GuestUserSessionResponse> =
        client.post(
            url = "/v3/auth/guest/session",
            headers =
            mapOf(
                Constants.CLIENT_KEY to Constants.CUSTOMER_APP_GUEST_KEY,
                "organizationId" to Constants.ORGANIZATION_ID
            ),
            body = GuestUserSessionRequest(deviceId = deviceId)
        )

    suspend fun getCategories(
        cityId: String = "ff6d619fb0d1b717432e9",
        customerChannel: String = "CONSUMER"
    ): ResultState<CategoriesResponseModel> {
        return client.get(
            "/v1/catalog-bff/categories",
            params = mapOf("customerChannel" to customerChannel, "cityId" to cityId)
        )
    }
}

object Constants {
    private const val GATEWAY_DOMAIN = "bazaar-api.bazaar.technology"
    private const val IDENTITY_GATEWAY_DOMAIN = "api.bazaar-identity.com"
    const val GATEWAY_URL = "https://$GATEWAY_DOMAIN/"
    const val IDENTITY_URL = "https://$IDENTITY_GATEWAY_DOMAIN/"
    const val CLIENT_KEY = "X-Bazaar-Client-Key"
    const val CUSTOMER_APP_GUEST_KEY =
        "8b623508563748979baed00871a29652193d5ad4eb6ea019f29faeb0eaac8897"
    const val ORGANIZATION_ID = "7931660281019108183210"
}

@Serializable
data class GuestUserSessionRequest(
    @SerialName("deviceId")
    var deviceId: String,
    @SerialName("userAttributes")
    var userAttributes: Map<String, String> = emptyMap()
)

@Serializable
data class GuestUserSessionResponse(
    @SerialName("token")
    val token: String,
    @SerialName("refreshToken")
    val refreshToken: String,
    @SerialName("expiresAt")
    val expiresAt: String
)
