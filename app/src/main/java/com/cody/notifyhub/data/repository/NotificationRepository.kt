package com.cody.notifyhub.data.repository

import com.cody.notifyhub.data.local.NotificationDao
import com.cody.notifyhub.data.local.NotificationEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class NotificationRepository @Inject constructor(
    private val dao: NotificationDao
) {
    // ---------- Read (Flows: live updates) ----------

    /** Empty query → all notifications; otherwise → matching ones */
    fun observe(query: String): Flow<List<NotificationEntity>> =
        if (query.isBlank()) dao.observeAll() else dao.search(query.trim())

    fun observeById(id: String): Flow<NotificationEntity?> = dao.observeById(id)

    fun observeUnreadCount(): Flow<Int> = dao.observeUnreadCount()

    // ---------- Write (suspend: one-time actions) ----------

    /** true = new message saved, false = duplicate ignored */
    suspend fun save(item: NotificationEntity): Boolean = dao.insert(item) != -1L

    suspend fun markAsRead(id: String) = dao.markAsRead(id)

    suspend fun markAllAsRead() = dao.markAllAsRead()

    suspend fun delete(id: String) = dao.delete(id)

    suspend fun clearAll() = dao.clearAll()
}