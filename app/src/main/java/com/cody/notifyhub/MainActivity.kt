package com.cody.notifyhub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.cody.notifyhub.ui.navigation.AppNavGraph
import com.cody.notifyhub.ui.theme.NotifyHubTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Shows the system "Allow notifications?" dialog and gives us the answer
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            Log.d("PERMISSION", "Notifications granted: $granted")
        }

    // Increases each time a notification is tapped while the app is already open.
    // Compose watches it, and AppNavGraph then goes back to the Inbox.
    private var openInboxSignal by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        logFcmToken()

        enableEdgeToEdge()
        setContent {
            NotifyHubTheme {
                AppNavGraph(openInboxSignal = openInboxSignal)
            }
        }
    }

    // Called instead of onCreate when the app is already open and a notification is tapped
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openInboxSignal++
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun logFcmToken() {
        // Prints this device's FCM "address". Copy it into send_test.py
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> Log.d("FCM_TOKEN", token) }
            .addOnFailureListener { e -> Log.e("FCM_TOKEN", "Failed to get token", e) }
    }
}