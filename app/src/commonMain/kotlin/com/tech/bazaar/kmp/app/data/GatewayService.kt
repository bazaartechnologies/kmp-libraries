package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.kmp.app.data.Constants.GATEWAY_URL
import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.PlatformContext
import com.tech.bazaar.network.api.ResultState
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GatewayService(platformContext: PlatformContext) {
    private val client: NetworkClient = NetworkClientBuilder()
        .sessionManager(AppSessionManager())
        .versioningProvider(AppVersionDetailsProviderImpl())
        .platformContext(platformContext)
        .clientConfig(
            NetworkClientBuilder.ClientConfig(
                isAuthorizationEnabled = true,
                isSslPinningEnabled = true,
                apiUrl = GATEWAY_URL,
                authUrl = GATEWAY_URL,
                enableDebugMode = true
            )
        )
        .build()

    suspend fun getGuestUserSession(
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
    const val GATEWAY_URL = "https://$GATEWAY_DOMAIN/"
    const val CLIENT_KEY = "X-Bazaar-Client-Key"
    const val CUSTOMER_APP_GUEST_KEY =
        ""
    const val ORGANIZATION_ID = ""
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
