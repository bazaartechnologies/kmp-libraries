package com.tech.bazaar.kmp.app

import androidx.compose.ui.window.ComposeUIViewController
import com.tech.bazaar.network.api.PlatformContext

fun MainViewController() = ComposeUIViewController { App(
    object : PlatformContext {
        override val context: Any?
            get() = null
    }
) }