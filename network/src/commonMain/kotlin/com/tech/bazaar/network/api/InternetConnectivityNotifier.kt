package com.tech.bazaar.network.api

import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest

class InternetConnectivityNotifier private constructor(private val connectivity: Connectivity) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val statusUpdates = connectivity.statusUpdates.mapLatest {
        when (it) {
            is Connectivity.Status.Connected -> InternetConnectivityStatus.Connected(it.isMetered)
            Connectivity.Status.Disconnected -> InternetConnectivityStatus.Disconnected
        }
    }

    suspend fun isMonitoring(): Boolean? {
        return connectivity.isMonitoring.firstOrNull()
    }

    suspend fun isConnected(): Boolean {
        return isMonitoring()?.let { connectivity.status().isConnected } ?: true
    }

    suspend fun isDisconnected(): Boolean {
        return isMonitoring()?.let { connectivity.status().isDisconnected } ?: false
    }

    suspend fun isMetered(): Boolean {
        return isMonitoring()?.let { connectivity.status().isMetered } ?: true
    }

    companion object {
        val instance = InternetConnectivityNotifier(
            connectivity = Connectivity {
                autoStart = true
            }
        )
    }
}

sealed interface InternetConnectivityStatus {
    data class Connected(val isMetered: Boolean) : InternetConnectivityStatus
    data object Disconnected : InternetConnectivityStatus
}