package com.tech.bazaar.network.di

interface SessionManager {

    fun getAuthToken(): String

    fun getRefreshToken(): String

    fun getUsername(): String

    fun onTokenRefreshed(token: String, expiresAt: String, refreshToken: String)

    fun onTokenExpires()

    fun shouldSendWithoutRequest(host: String) = host == "bazaar-api.bazaar.technology"

    @Deprecated(
        "No need of this, network is handling the app " +
            "version name and version code internally, will removed this method in future"
    )
    fun getAppVersion(): String = ""
}
