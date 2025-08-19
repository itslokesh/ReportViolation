package com.example.reportviolation.utils.push

import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.reportviolation.MainActivity
import com.example.reportviolation.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class AppFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"] ?: message.notification?.title ?: getString(R.string.notifications)
        val body = message.data["message"] ?: message.notification?.body ?: ""

        PushNotifications.ensureChannel(this)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, PushNotifications.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), builder.build())
        }

        // Broadcast to refresh notifications tab
        sendBroadcast(Intent(ACTION_REFRESH_NOTIFICATIONS))
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Optionally send FCM token to backend if needed later
    }

    companion object {
        const val ACTION_REFRESH_NOTIFICATIONS = "com.example.reportviolation.REFRESH_NOTIFICATIONS"
    }
}


