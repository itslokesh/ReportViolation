package com.example.reportviolation

import android.app.Application
import com.example.reportviolation.di.AppModule

class ReportViolationApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize dependency injection
        AppModule.initialize(this)
    }
} 