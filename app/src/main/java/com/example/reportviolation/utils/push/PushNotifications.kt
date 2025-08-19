package com.example.reportviolation.utils.push

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object PushNotifications {
    const val CHANNEL_ID = "traffic_violation_channel"
    private const val CHANNEL_NAME = "Traffic Updates"
    private const val CHANNEL_DESC = "Notifications about report status and updates"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = CHANNEL_DESC
            nm.createNotificationChannel(channel)
        }
    }
}


