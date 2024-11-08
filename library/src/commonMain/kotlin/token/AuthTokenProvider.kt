package com.bazaartech.core_network.token

internal interface AuthTokenProvider {
    fun getAuthToken(): String
}
