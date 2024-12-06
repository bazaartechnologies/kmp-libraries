package com.tech.bazaar.network.utils

interface AppVersionDetailsProvider {
    fun getAppVersionName(): String

    fun getAppVersionCode(): String
}

