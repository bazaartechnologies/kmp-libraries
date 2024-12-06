package com.tech.bazaar.network.di

import com.tech.bazaar.network.api.BaseUrls
import com.tech.bazaar.network.api.CertTransparencyFlagProvider
import com.tech.bazaar.network.api.NetworkExternalDependencies
import org.koin.core.module.Module
import org.koin.dsl.module

val NetworkDependenciesModule: Module = module {

    single<com.tech.bazaar.network.api.BaseUrls> { get<com.tech.bazaar.network.api.NetworkExternalDependencies>().getBaseUrls() }

//    single { get<NetworkExternalDependencies>().getSessionManager() }

//    single<NetworkEventLogger> { get<NetworkExternalDependencies>().getNetworkEventLogger() }
//
//    single<NetworkApiExceptionLogger> { get<NetworkExternalDependencies>().getNetworkExceptionLogger() }
//
    single<com.tech.bazaar.network.api.CertTransparencyFlagProvider> { get<com.tech.bazaar.network.api.NetworkExternalDependencies>().getCertTransparencyFlagProvider() }
}
