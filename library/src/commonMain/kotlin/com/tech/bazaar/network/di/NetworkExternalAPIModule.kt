package com.tech.bazaar.network.di

import com.tech.bazaar.network.api.NetworkExternalAPI
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module


val NetworkExternalAPIModule: Module = module {
    factory<com.tech.bazaar.network.api.NetworkExternalAPI> {
        val httpClientMain: HttpClient = get(named("MainGateway"))
        val httpClientSecure: HttpClient = get(named("SecureGateway"))

        object : com.tech.bazaar.network.api.NetworkExternalAPI {
            override fun createServiceOnMainGateway(): HttpClient = httpClientMain
            override fun createServiceOnSecureGateway(): HttpClient = httpClientSecure
        }
    }


//todo this will be added in app iOS and android


//    single<FeatureFlagService> {
//        FeatureFlagServiceImpl(client = get<NetworkExternalAPI>().createServiceOnMainGateway())
//    }

//    interface FeatureFlagService {
//        suspend fun getFeatureFlags(): List<NetworkFeatureFlag>
//    }
//
//    class FeatureFlagServiceImpl(private val client: HttpClient) : FeatureFlagService {
//        override suspend fun getFeatureFlags(): List<NetworkFeatureFlag> {
//            return client.get("/v1/feature/allowed")
//        }
//    }
}


