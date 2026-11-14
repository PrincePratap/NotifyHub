package com.cody.notifyhub.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(item: NotificationEntity): Long   // returns -1 if the id already exists

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE id = :id")
    fun observeById(id: String): Flow<NotificationEntity?>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("UPDATE notifications SET isRead = 1 WHERE isRead = 0")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM notifications")
    suspend fun clearAll()

    @Query("""
    SELECT * FROM notifications
    WHERE title LIKE '%' || :query || '%'
       OR description LIKE '%' || :query || '%'
    ORDER BY timestamp DESC
""")
    fun search(query: String): Flow<List<NotificationEntity>>
}