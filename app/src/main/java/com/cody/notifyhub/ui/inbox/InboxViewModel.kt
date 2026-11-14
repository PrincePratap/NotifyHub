package com.cody.notifyhub.ui.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class InboxUiState(
    val items: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = true
)

@HiltViewModel
class InboxViewModel @Inject constructor(
    repository: NotificationRepository
) : ViewModel() {

    val uiState: StateFlow<InboxUiState> =
        combine(
            repository.observeAll(),
            repository.observeUnreadCount()
        ) { items, unread ->
            InboxUiState(items = items, unreadCount = unread, isLoading = false)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InboxUiState()          // isLoading = true until Room answers
        )
}