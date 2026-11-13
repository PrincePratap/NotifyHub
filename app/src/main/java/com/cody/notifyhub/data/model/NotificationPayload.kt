package com.cody.notifyhub.data.model

import com.cody.notifyhub.data.local.NotificationEntity

object NotificationPayload {
    fun toEntity(data: Map<String, String>): NotificationEntity? {
        val id = data["id"]?.takeIf { it.isNotBlank() } ?: return null   // no id → drop it

        return NotificationEntity(
            id = id,
            title = data["title"]?.takeIf { it.isNotBlank() } ?: "New notification",
            description = data["description"].orEmpty(),
            bannerImg = data["bannerImg"]?.takeIf { it.startsWith("http") },  // invalid URL → null
            timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis()
        )
    }
}