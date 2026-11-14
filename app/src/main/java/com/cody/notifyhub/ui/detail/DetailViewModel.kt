package com.cody.notifyhub.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: NotificationRepository
) : ViewModel() {

    // "id" must match the navigation route argument: detail/{id}
    private val id: String = checkNotNull(savedStateHandle["id"])

    val uiState: StateFlow<DetailUiState> =
        repository.observeById(id)
            .map { item -> if (item == null) DetailUiState.NotFound else DetailUiState.Success(item) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState.Loading)

    init {
        // Opening Details marks it read. Runs once per screen, not on every rotation
        viewModelScope.launch { repository.markAsRead(id) }
    }

    /**
     * Deletes this notification, then calls [onDone] (used to navigate back).
     * We navigate only AFTER the delete finishes: if we went back first, this
     * ViewModel would be destroyed and viewModelScope cancelled, possibly
     * before the delete had run.
     */
    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.delete(id)
            onDone()
        }
    }
}

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(val item: NotificationEntity) : DetailUiState
    data object NotFound : DetailUiState
}