package com.example.samaajbot

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.example.samaajbot.data.api.SamaajBotApi
import com.example.samaajbot.data.models.FCMTokenRequest
import com.example.samaajbot.ui.navigation.NavGraph
import com.example.samaajbot.ui.theme.SamaajBotTheme
import com.example.samaajbot.utils.SessionManager
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var api: SamaajBotApi

    // Permission launcher for Android 13+ (API 33+)
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) fetchAndSendFcmToken()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Extract community_id from notification tap (if app opened via notification)
        // -1 means app opened normally, not from notification
        val notificationCommunityId = intent.getIntExtra("community_id", -1)

        // Request notification permission on Android 13+
        askNotificationPermission()

        setContent {
            SamaajBotTheme {
                NavGraph(
                    sessionManager          = sessionManager,
                    notificationCommunityId = notificationCommunityId
                )
            }
        }
    }

    // Called when app is already open and a new notification arrives
    // and user taps it
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val communityId = intent.getIntExtra("community_id", -1)
        if (communityId != -1) {
            // Restart with new intent so NavGraph picks up the community_id
            recreate()
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    fetchAndSendFcmToken()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            fetchAndSendFcmToken()
        }
    }

    private fun fetchAndSendFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        api.updateFcmToken(FCMTokenRequest(token))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}