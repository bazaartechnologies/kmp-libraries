package com.tech.bazaar.kmp.app.data

import com.tech.bazaar.network.api.AppVersionDetailsProvider

class AppVersionDetailsProviderImpl : AppVersionDetailsProvider {
    override fun getAppVersionName(): String {
        return "1.0.0"
    }

    override fun getAppVersionCode(): String {
        return "1"
    }
}