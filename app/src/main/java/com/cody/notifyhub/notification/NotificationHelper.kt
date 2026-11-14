package com.cody.notifyhub.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.cody.notifyhub.MainActivity
import com.cody.notifyhub.R
import com.cody.notifyhub.data.local.NotificationEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import android.graphics.Bitmap
import coil3.SingletonImageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import kotlinx.coroutines.withTimeoutOrNull

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "assignment"
        const val CHANNEL_NAME = "Assignment"
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun show(item: NotificationEntity) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, item.id.hashCode(), intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(item.title)
            .setContentText(item.description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val bitmap = loadBitmap(item.bannerImg)
        if (bitmap != null) {
            builder
                .setLargeIcon(bitmap)                                   // thumbnail when collapsed
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(bitmap)                             // full banner when expanded
                        .bigLargeIcon(null as Bitmap?)                  // hide the thumbnail when expanded
                )
        }
        // bitmap == null (no URL, bad URL, timeout) → text-only notification

        manager.notify(item.id.hashCode(), builder.build())
    }

    private suspend fun loadBitmap(url: String?): Bitmap? {
        if (url.isNullOrBlank()) return null
        return withTimeoutOrNull(5_000) {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false)
                .build()
            val result = SingletonImageLoader.get(context).execute(request)
            (result as? SuccessResult)?.image?.toBitmap()
        }
    }
}