package com.example.samaajbot.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.samaajbot.MainActivity
import com.example.samaajbot.R
import com.example.samaajbot.data.api.SamaajBotApi
import com.example.samaajbot.data.models.FCMTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Instead of @AndroidEntryPoint, use EntryPoint interface
// This is the correct way to inject into non-Android components
@EntryPoint
@InstallIn(SingletonComponent::class)
interface FirebaseServiceEntryPoint {
    fun samaajBotApi(): SamaajBotApi
}

class SamaajFirebaseService : FirebaseMessagingService() {

    // Get API using EntryPointAccessors instead of @Inject
    private val api: SamaajBotApi by lazy {
        EntryPointAccessors.fromApplication(
            applicationContext,
            FirebaseServiceEntryPoint::class.java
        ).samaajBotApi()
    }

    companion object {
        const val CHANNEL_ID   = "samaajbot_notifications"
        const val CHANNEL_NAME = "SamaajBot Notifications"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                api.updateFcmToken(FCMTokenRequest(token))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title       = remoteMessage.notification?.title ?: "SamaajBot"
        val body        = remoteMessage.notification?.body  ?: ""
        val communityId = remoteMessage.data["community_id"]?.toIntOrNull() ?: -1

        showNotification(title, body, communityId)
    }

    private fun showNotification(title: String, body: String, communityId: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (communityId != -1) {
                putExtra("community_id", communityId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            communityId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new documents and community updates"
                enableVibration(true)
            }
            val manager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}