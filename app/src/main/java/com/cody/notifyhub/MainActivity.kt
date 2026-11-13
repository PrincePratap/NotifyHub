package com.cody.notifyhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.data.repository.NotificationRepository
import com.cody.notifyhub.ui.theme.NotifyHubTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint                                   // lets Hilt inject into this Activity
class MainActivity : ComponentActivity() {

    @Inject lateinit var repo: NotificationRepository   // TEST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TEST: insert one fake row
        lifecycleScope.launch {
            val isNew = repo.save(
                NotificationEntity(
                    id = "test_1",
                    title = "Hello",
                    description = "First row",
                    bannerImg = null,
                    timestamp = System.currentTimeMillis()
                )
            )
            android.util.Log.d("DB_TEST", "Saved new row? $isNew")
        }

        enableEdgeToEdge()
        setContent {
            NotifyHubTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text("NotifyHub", modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}