package com.tech.bazaar.kmp.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun ListScreen(
    navigateToDetails: (DetailDestination) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEach {
            Button(
                onClick = { navigateToDetails.invoke(it) }
            ) {
                Text(it.name)
            }
        }
    }
}

val items = mapOf(
    1 to "Item 1",
    2 to "Item 2",
    3 to "Item 3",
    4 to "Item 4",
    5 to "Item 5",
    6 to "Item 6",
    7 to "Item 7",
    8 to "Item 8"
).map {
    DetailDestination(it.key, it.value)
}