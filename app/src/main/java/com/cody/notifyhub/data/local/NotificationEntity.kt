package com.cody.notifyhub.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,      // the payload's id → no duplicates possible
    val title: String,
    val description: String,
    val bannerImg: String?,          // nullable: the image is optional
    val timestamp: Long,
    val isRead: Boolean = false      // new messages start unread
)