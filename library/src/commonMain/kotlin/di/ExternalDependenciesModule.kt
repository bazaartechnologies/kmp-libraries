package com.bazaartech.core_network.di

import com.bazaartech.core_network.api.BaseUrls
import com.bazaartech.core_network.api.CertTransparencyFlagProvider
import com.bazaartech.core_network.api.NetworkApiExceptionLogger
import com.bazaartech.core_network.api.NetworkEventLogger
import com.bazaartech.core_network.api.NetworkExternalDependencies
import com.bazaartech.core_network.api.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ExternalDependenciesModule {

    @Singleton
    @Provides
    internal fun provideBaseUrls(dependencies: NetworkExternalDependencies): BaseUrls =
        dependencies.getBaseUrls()

    @Provides
    internal fun provideSessionManager(dependencies: NetworkExternalDependencies): SessionManager =
        dependencies.getSessionManager()

    @Singleton
    @Provides
    internal fun provideNetworkEventLogger(dependencies: NetworkExternalDependencies): NetworkEventLogger =
        dependencies.getNetworkEventLogger()

    @Singleton
    @Provides
    internal fun provideNetworkExceptionLogger(dependencies: NetworkExternalDependencies): NetworkApiExceptionLogger =
        dependencies.getNetworkExceptionLogger()

    @Singleton
    @Provides
    internal fun supplyCertTransparencyFlagProvider(
        dependencies: NetworkExternalDependencies
    ): CertTransparencyFlagProvider =
        dependencies.getCertTransparencyFlagProvider()
}
