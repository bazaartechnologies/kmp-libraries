package com.tech.bazaar.network.api

import com.tech.bazaar.network.di.SessionManager

interface NetworkExternalDependencies {

    fun getBaseUrls(): com.tech.bazaar.network.api.BaseUrls

    fun getSessionManager(): SessionManager

//    fun getNetworkEventLogger(): NetworkEventLogger
//
//    fun getNetworkExceptionLogger(): NetworkApiExceptionLogger
//
    fun getCertTransparencyFlagProvider(): com.tech.bazaar.network.api.CertTransparencyFlagProvider
}
