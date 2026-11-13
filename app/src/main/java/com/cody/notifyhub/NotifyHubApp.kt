package com.cody.notifyhub

import android.app.Application
import com.cody.notifyhub.notification.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class NotifyHubApp : Application() {

    @Inject
    lateinit var notificationHelper: NotificationHelper

    override fun onCreate() {
        super.onCreate()                   // Hilt injects the field here, so call it first
        notificationHelper.createChannel()
    }
}