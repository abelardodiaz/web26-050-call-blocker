package com.redv6.callblocker

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CallBlockerApplication : Application() {

    companion object {
        private const val TAG = "CallBlockerApp"
    }

    override fun onCreate() {
        Log.d(TAG, "=== APPLICATION onCreate START ===")
        Log.d(TAG, "API Level: ${android.os.Build.VERSION.SDK_INT}")
        Log.d(TAG, "Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")

        try {
            super.onCreate()
            Log.d(TAG, "=== APPLICATION onCreate SUCCESS ===")
        } catch (e: Exception) {
            Log.e(TAG, "=== APPLICATION onCreate FAILED ===", e)
            throw e
        }
    }

    override fun onTerminate() {
        Log.d(TAG, "=== APPLICATION onTerminate ===")
        super.onTerminate()
    }
}
