package com.cody.notifyhub.fcm

import android.util.Log
import com.cody.notifyhub.data.model.NotificationPayload
import com.cody.notifyhub.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject
import com.cody.notifyhub.notification.NotificationHelper
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class InboxMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var repository: NotificationRepository
    @Inject lateinit var notificationHelper: NotificationHelper

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data.toMutableMap()
        message.notification?.let { n ->                   // Console-style message fallback
            data.putIfAbsent("title", n.title.orEmpty())
            data.putIfAbsent("description", n.body.orEmpty())
        }

        val entity = NotificationPayload.toEntity(data)
        if (entity == null) {
            Log.w(TAG, "Dropped message without id: $data")
            return
        }

        val isNew = runBlocking { repository.save(entity) }
        if (isNew) notificationHelper.show(entity) else Log.d(TAG, "Duplicate ignored: ${entity.id}")
    }

    companion object { private const val TAG = "FCM" }
}