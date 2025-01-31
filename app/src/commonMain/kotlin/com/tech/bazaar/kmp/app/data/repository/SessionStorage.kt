package com.tech.bazaar.kmp.app.data.repository

class SessionStorage {
    private val data: MutableMap<String, Any?> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        data[key]?.let {
            return it as? T
        }
        return null
    }

    fun <T> set(
        key: String,
        value: T?
    ) {
        data[key] = value
    }

    fun clear() {
        data.clear()
    }

    companion object {
        const val ACCESS_TOKEN = "access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USERNAME = "username"
    }
}