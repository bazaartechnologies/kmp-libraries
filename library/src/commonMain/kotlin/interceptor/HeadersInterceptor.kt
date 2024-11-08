package com.bazaartech.core_network.interceptor

import com.bazaartech.core_network.token.AuthTokenProvider
import com.bazaartech.core_network.utils.AppVersionDetailsProvider


internal class HeadersInterceptor constructor(
    private val tokenProvider: AuthTokenProvider,
    private val versionDetailsProvider: AppVersionDetailsProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header(AUTHORIZATION, "$BEARER ${tokenProvider.getAuthToken()}")
                .header(APP_VERSION, versionDetailsProvider.getAppVersionCode())
                .header(APP_VERSION_NAME, versionDetailsProvider.getAppVersionName())
                .header(APP_VERSION_CODE, versionDetailsProvider.getAppVersionCode())
                .build()
        )
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
        const val APP_VERSION = "AppVersion"
        const val APP_VERSION_NAME = "AppVersionName"
        const val APP_VERSION_CODE = "AppVersionCode"
        const val BEARER = "Bearer"
    }
}
