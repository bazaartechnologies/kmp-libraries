package com.tech.bazaar.kmp.app

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DetailsScreen(
    id: Int,
    name: String,
    navigateBack: () -> Unit
) {
    Button(
        onClick = navigateBack
    ) {
        Text("Go back from $name")
    }
}