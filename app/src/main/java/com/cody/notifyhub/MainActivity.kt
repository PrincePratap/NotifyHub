package com.cody.notifyhub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        askNotificationPermission()
        logFcmToken()

        enableEdgeToEdge()
        setContent {
            NotifyHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text("NotifyHub", modifier = Modifier.padding(innerPadding))
                }
            }
        }
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