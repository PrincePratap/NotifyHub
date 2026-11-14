package com.cody.notifyhub.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.data.repository.FcmTokenRepository
import com.cody.notifyhub.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortOrder(val label: String) {
    NEWEST("Newest first"),
    OLDEST("Oldest first"),
    UNREAD_FIRST("Unread first")
}

data class InboxUiState(
    val items: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true,
    val query: String = "",
    val sortOrder: SortOrder = SortOrder.NEWEST
)

@OptIn(ExperimentalCoroutinesApi::class)   // needed for flatMapLatest
@HiltViewModel
class InboxViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val tokenRepository: FcmTokenRepository
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sortOrder = MutableStateFlow(SortOrder.NEWEST)

    val uiState: StateFlow<InboxUiState> =
        combine(
            query.flatMapLatest { q -> repository.observe(q) },  // DB filters (search)
            repository.observeUnreadCount(),
            query,
            sortOrder
        ) { items, unread, q, sort ->
            InboxUiState(
                items = items.applySort(sort),                    // ViewModel sorts
                unreadCount = unread,
                isLoading = false,
                query = q,
                sortOrder = sort
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InboxUiState()
        )

    fun onQueryChange(newQuery: String) { query.value = newQuery }

    fun onSortChange(order: SortOrder) { sortOrder.value = order }

    fun markAllAsRead() {
        viewModelScope.launch { repository.markAllAsRead() }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAll() }
    }

    suspend fun getToken(): String? = tokenRepository.getToken()

    private fun List<NotificationEntity>.applySort(order: SortOrder): List<NotificationEntity> =
        when (order) {
            SortOrder.NEWEST -> sortedByDescending { it.timestamp }
            SortOrder.OLDEST -> sortedBy { it.timestamp }
            SortOrder.UNREAD_FIRST -> sortedWith(
                compareBy<NotificationEntity> { it.isRead }        // false (unread) first
                    .thenByDescending { it.timestamp }              // then newest within each group
            )
        }
}