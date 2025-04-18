package com.tech.bazaar.network.http

import android.content.Context
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.core.FlipperClient
import com.facebook.flipper.plugins.crashreporter.CrashReporterPlugin
import com.facebook.flipper.plugins.databases.DatabasesFlipperPlugin
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.navigation.NavigationFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import okhttp3.Interceptor

internal object FlipperInterceptorFactory {

    internal fun createInterceptor(context: Context): Interceptor? {
        return getFlipperClient(context)?.getPlugin<NetworkFlipperPlugin>(NetworkFlipperPlugin.ID)
            ?.let { FlipperOkhttpInterceptor(it) }
    }

    private fun getFlipperClient(
        context: Context
    ): FlipperClient? {
        if (!FlipperUtils.shouldEnableFlipper(context)) {
            return null
        }

        com.facebook.soloader.SoLoader.init(context, false)
        return AndroidFlipperClient.getInstance(context).apply {
            addPlugin(CrashReporterPlugin.getInstance())
            addPlugin(DatabasesFlipperPlugin(context))
            addPlugin(NavigationFlipperPlugin.getInstance())
            addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
            addPlugin(NetworkFlipperPlugin())
            start()
        }
    }
}