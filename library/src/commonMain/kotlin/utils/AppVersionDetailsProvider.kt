package com.bazaartech.core_network.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface AppVersionDetailsProvider {
    fun getAppVersionName(): String

    fun getAppVersionCode(): String
}

class AppVersionDetailsProviderImp @Inject constructor(@ApplicationContext val context: Context) :
    AppVersionDetailsProvider {
    override fun getAppVersionName(): String {
        return getVersionDetail().first
    }

    override fun getAppVersionCode(): String {
        return getVersionDetail().second.toString()
    }

    private fun getVersionDetail(): Pair<String, Int> {
        try {
            val pInfo: PackageInfo =
                context.packageManager.getPackageInfo(context.packageName, 0)

            return Pair(pInfo.versionName, pInfo.versionCode)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(this::javaClass.name, e.message.toString())
        }
        return Pair("", 0)
    }
}
