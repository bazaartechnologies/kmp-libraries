package com.tech.bazaar.network.http

import android.content.Context
import com.facebook.flipper.core.FlipperClient
import okhttp3.Interceptor

internal object FlipperInterceptorFactory {

    internal fun createInterceptor(context: Context): Interceptor? {
        return null
    }

    private fun getFlipperClient(
        context: Context
    ): FlipperClient? {
        return null
    }
}
