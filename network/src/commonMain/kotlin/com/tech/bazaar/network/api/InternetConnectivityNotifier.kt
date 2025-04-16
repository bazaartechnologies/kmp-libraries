package com.tech.bazaar.network.api

import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

interface InternetConnectivityNotifier {
    val statusUpdates: Flow<InternetConnectivityStatus>
    suspend fun getCurrentStatus(): InternetConnectivityStatus
    suspend fun isMonitoring(): Boolean?
    suspend fun isConnected(): Boolean
    suspend fun isDisconnected(): Boolean
    suspend fun isMetered(): Boolean
}

class DefaultInternetConnectivityNotifier private constructor(
    private val connectivity: Connectivity
) : InternetConnectivityNotifier {
    override val statusUpdates = connectivity.statusUpdates
        .map { it.toInternetConnectivityStatus() }
        .flowOn(Dispatchers.IO)

    override suspend fun getCurrentStatus(): InternetConnectivityStatus {
        return connectivity.status().toInternetConnectivityStatus()
    }

    override suspend fun isMonitoring(): Boolean? {
        return connectivity.isMonitoring.firstOrNull()
    }

    override suspend fun isConnected(): Boolean {
        return isMonitoring()?.let { connectivity.status().isConnected } ?: true
    }

    override suspend fun isDisconnected(): Boolean {
        return isMonitoring()?.let { connectivity.status().isDisconnected } ?: false
    }

    override suspend fun isMetered(): Boolean {
        return isMonitoring()?.let { connectivity.status().isMetered } ?: true
    }

    companion object {
        val instance = DefaultInternetConnectivityNotifier(
            connectivity = Connectivity {
                autoStart = true
            }
        )
    }
}

private fun Connectivity.Status.toInternetConnectivityStatus(): InternetConnectivityStatus {
    return when (this) {
        is Connectivity.Status.Connected -> InternetConnectivityStatus.Connected(isMetered)
        Connectivity.Status.Disconnected -> InternetConnectivityStatus.Disconnected
    }
}

sealed interface InternetConnectivityStatus {
    data class Connected(val isMetered: Boolean) : InternetConnectivityStatus
    data object Disconnected : InternetConnectivityStatus
}