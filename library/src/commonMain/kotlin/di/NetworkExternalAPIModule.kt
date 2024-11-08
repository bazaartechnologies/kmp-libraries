package com.bazaartech.core_network.di

import com.bazaartech.core_network.api.NetworkExternalAPI
import com.bazaartech.core_network.qualifiers.MainGateway
import com.bazaartech.core_network.qualifiers.SecureGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
class NetworkExternalAPIModule {

    @Provides
    fun provideExternalAPI(
        @MainGateway retrofitMain: Retrofit,
        @SecureGateway retrofitSecure: Retrofit
    ): NetworkExternalAPI {
        return object : NetworkExternalAPI {
            override fun <T> createServiceOnMainGateway(type: Class<T>): T {
                return retrofitMain.create(type)
            }

            override fun <T> createServiceOnSecureGateway(type: Class<T>): T {
                return retrofitSecure.create(type)
            }
        }
    }
}
