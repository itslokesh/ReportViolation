package com.example.reportviolation

import android.app.Application
import com.example.reportviolation.data.remote.auth.TokenPrefs
import com.example.reportviolation.di.AppModule

class ReportViolationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize dependency injection
        AppModule.initialize(this)
        // Initialize encrypted token storage (loads tokens into TokenStore on startup)
        try {
            TokenPrefs.init(this)
        } catch (_: Throwable) { }

        // Schedule periodic cache cleanup to prevent excessive storage/RAM usage
        try {
            com.example.reportviolation.data.remote.CacheManager.schedulePeriodicCleanup(this)
        } catch (_: Throwable) { }

        // Create notification channel
        try {
            com.example.reportviolation.utils.push.PushNotifications.ensureChannel(this)
        } catch (_: Throwable) { }
    }
} 