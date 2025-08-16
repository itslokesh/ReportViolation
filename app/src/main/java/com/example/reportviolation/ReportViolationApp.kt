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
    }
} 