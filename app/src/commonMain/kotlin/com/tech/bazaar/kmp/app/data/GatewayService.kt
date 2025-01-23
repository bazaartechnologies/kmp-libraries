package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.kmp.app.data.Constants.GATEWAY_URL
import com.tech.bazaar.network.api.NetworkClient
import com.tech.bazaar.network.api.NetworkClientBuilder
import com.tech.bazaar.network.api.ResultState

class GatewayService {
   private val client: NetworkClient = NetworkClientBuilder()
        .sessionManager(SessionManagerImpl())
        .versioningProvider(AppVersionDetailsProviderImpl())
        .clientConfig(
            NetworkClientBuilder.ClientConfig(
                isAuthorizationEnabled = true,
                isSslPinningEnabled = true,
                apiUrl = GATEWAY_URL,
                authUrl = GATEWAY_URL
            )
        )
        .build()

    suspend fun getCategories(
        cityId: String = "ff6d619fb0d1b717432e9",
        customerChannel: String = "CONSUMER"
    ): ResultState<CategoriesResponseModelV2> {
        return client.get(
            "/v1/catalog-bff/categories",
            params = mapOf("customerChannel" to customerChannel, "cityId" to cityId)
        )
    }
}

object Constants {
    private const val GATEWAY_DOMAIN = "bazaar-api.bazaar.technology"
    const val GATEWAY_URL = "https://$GATEWAY_DOMAIN/"
}
