package com.tech.bazaar.network.token

interface AuthTokenProvider {
    fun getAuthToken(): String
}
