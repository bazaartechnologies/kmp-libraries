package com.tech.bazaar.kmp.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tech.bazaar.kmp.app.presentation.CategoriesUiState
import com.tech.bazaar.kmp.app.presentation.CategoriesViewModel
import com.tech.bazaar.network.api.DefaultInternetConnectivityNotifier
import com.tech.bazaar.network.api.InternetConnectivityNotifier
import com.tech.bazaar.network.api.InternetConnectivityStatus

@Composable
fun ListScreen(
    categoryViewModel: CategoriesViewModel,
    navigateToDetails: (DetailDestination) -> Unit
) {
    val uiState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val connectionState by DefaultInternetConnectivityNotifier.instance.statusUpdates.collectAsStateWithLifecycle(
        InternetConnectivityStatus.Disconnected
    )

    Column(modifier = Modifier.padding(top = 48.dp), horizontalAlignment = Alignment.Start) {
        Text("Connection Status = $connectionState")
        Categories(uiState)
    }
}

@Composable
private fun Categories(uiState: CategoriesUiState) {
    when (uiState) {
        is CategoriesUiState.Error -> println("Error")
        CategoriesUiState.Loading -> println("Loading")
        is CategoriesUiState.Success -> {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.categories.forEach {
                    item {
                        Button(onClick = {}) {
                            Text(it.title)
                        }
                    }
                }
            }

        }
    }
}

