package com.cody.notifyhub.data.repository

import com.cody.notifyhub.data.local.NotificationDao
import com.cody.notifyhub.data.local.NotificationEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val dao: NotificationDao
) {
    fun observeAll() = dao.observeAll()
    fun observeById(id: String) = dao.observeById(id)
    fun observeUnreadCount() = dao.observeUnreadCount()

    suspend fun markAsRead(id: String) = dao.markAsRead(id)

    /** true = new message saved, false = duplicate ignored */
    suspend fun save(item: NotificationEntity): Boolean = dao.insert(item) != -1L
}