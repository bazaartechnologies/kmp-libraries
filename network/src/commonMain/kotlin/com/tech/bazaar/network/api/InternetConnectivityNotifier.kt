package com.tech.bazaar.network.api

import dev.jordond.connectivity.Connectivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.mapLatest

interface InternetConnectivityNotifier {
    val statusUpdates: Flow<InternetConnectivityStatus>
    suspend fun isMonitoring(): Boolean?
    suspend fun isConnected(): Boolean
    suspend fun isDisconnected(): Boolean
    suspend fun isMetered(): Boolean
}

class DefaultInternetConnectivityNotifier private constructor(
    private val connectivity: Connectivity
) : InternetConnectivityNotifier {
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override val statusUpdates = connectivity.statusUpdates
        .distinctUntilChanged()
        .debounce(1000L)
        .mapLatest {
            when (it) {
                is Connectivity.Status.Connected -> InternetConnectivityStatus.Connected(it.isMetered)
                Connectivity.Status.Disconnected -> InternetConnectivityStatus.Disconnected
            }
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

sealed interface InternetConnectivityStatus {
    data class Connected(val isMetered: Boolean) : InternetConnectivityStatus
    data object Disconnected : InternetConnectivityStatus
}