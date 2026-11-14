package com.cody.notifyhub.ui.inbox

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel


// Collect StateFlow as Compose State, respecting Android lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle

// Delegate access to the current state value using 'by'
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.ui.components.BannerImage
import com.cody.notifyhub.util.TimeFormatter

@Composable
fun InboxRoute(
    onOpenDetail: (String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    InboxScreen(state = state, onOpenDetail = onOpenDetail)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(state: InboxUiState, onOpenDetail: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(if (state.unreadCount > 0) "Inbox (${state.unreadCount} unread)" else "Inbox")
            })
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.items.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text("No notifications yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(contentPadding = padding) {
                items(state.items, key = { it.id }) { item ->      // key = stable rows, smooth updates
                    NotificationRow(item, onClick = { onOpenDetail(item.id) })
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationEntity, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BannerImage(item.bannerImg, Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                item.title,
                fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Text(
                TimeFormatter.relative(item.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!item.isRead) {                                 // unread dot
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
        }
    }
}