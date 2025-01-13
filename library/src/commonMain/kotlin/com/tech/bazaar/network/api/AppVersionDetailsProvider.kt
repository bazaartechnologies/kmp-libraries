package com.tech.bazaar.network.api

interface AppVersionDetailsProvider {
    fun getAppVersionName(): String

    fun getAppVersionCode(): String
}

