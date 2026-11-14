package com.cody.notifyhub.ui.inbox

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.ui.components.BannerImage
import com.cody.notifyhub.ui.theme.NotifyHubTheme
import com.cody.notifyhub.util.TimeFormatter
import kotlinx.coroutines.launch

// ---------- Route: connects to the ViewModel ----------

@Composable
fun InboxRoute(
    onOpenDetail: (String) -> Unit,
    viewModel: InboxViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    InboxScreen(
        state = state,
        onOpenDetail = onOpenDetail,
        onQueryChange = viewModel::onQueryChange,
        onSortChange = viewModel::onSortChange,
        onMarkAllRead = viewModel::markAllAsRead,
        onClearAll = viewModel::clearAll,
        onCopyToken = {
            scope.launch {
                val token = viewModel.getToken()
                if (token == null) {
                    Toast.makeText(context, "Couldn't get token. Check your internet.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val clipboard = context.getSystemService(ClipboardManager::class.java)
                clipboard.setPrimaryClip(ClipData.newPlainText("FCM token", token))
                // Android 13+ shows its own "Copied" message
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    Toast.makeText(context, "FCM token copied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )
}

// ---------- Screen: pure UI, previewable ----------

@Composable
fun InboxScreen(
    state: InboxUiState,
    onOpenDetail: (String) -> Unit,
    onQueryChange: (String) -> Unit = {},
    onSortChange: (SortOrder) -> Unit = {},
    onMarkAllRead: () -> Unit = {},
    onClearAll: () -> Unit = {},
    onCopyToken: () -> Unit = {}
) {
    var confirmClear by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            InboxHeader(
                state = state,
                onQueryChange = onQueryChange,
                onMarkAllRead = onMarkAllRead,
                onClearClick = { confirmClear = true },
                onCopyToken = onCopyToken
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                state.items.isEmpty() && state.query.isNotBlank() ->
                    NoResults(state.query, Modifier.align(Alignment.Center))

                state.items.isEmpty() ->
                    EmptyInbox(Modifier.align(Alignment.Center))

                else -> Column(Modifier.fillMaxSize()) {
                    SortChips(
                        selected = state.sortOrder,
                        onSelect = onSortChange
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.items, key = { it.id }) { item ->
                            NotificationCard(item = item, onClick = { onOpenDetail(item.id) })
                        }
                    }
                }
            }
        }
    }

    // Clearing can't be undone, so ask first
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear inbox?") },
            text = { Text("All notifications will be deleted. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { confirmClear = false; onClearAll() }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------- Header: colored app bar + search ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InboxHeader(
    state: InboxUiState,
    onQueryChange: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onClearClick: () -> Unit,
    onCopyToken: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    val showSearch = state.query.isNotBlank() || state.items.isNotEmpty()

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,                  // the Surface paints the color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Inbox", fontWeight = FontWeight.SemiBold)
                        if (state.unreadCount > 0) {
                            Spacer(Modifier.width(10.dp))
                            UnreadBadge(state.unreadCount)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Mark all as read") },
                            enabled = state.unreadCount > 0,
                            onClick = { menuOpen = false; onMarkAllRead() }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear inbox") },
                            enabled = state.items.isNotEmpty(),
                            onClick = { menuOpen = false; onClearClick() }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Copy FCM token") },
                            onClick = { menuOpen = false; onCopyToken() }
                        )
                    }
                }
            )

            if (showSearch) {
                SearchField(
                    query = state.query,
                    onQueryChange = onQueryChange,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                )
            } else {
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    // White pill on the indigo header
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        Text(
            text = "$count new",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search notifications") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(28.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

// ---------- Sort chips ----------

@Composable
private fun SortChips(selected: SortOrder, onSelect: (SortOrder) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())     // scrolls on small screens
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortOrder.entries.forEach { order ->
            val isSelected = order == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(order) },
                label = { Text(order.label) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

// ---------- List item ----------

@Composable
private fun NotificationCard(item: NotificationEntity, onClick: () -> Unit) {
    val unread = !item.isRead

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unread) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (unread) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = if (unread) 2.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            BannerImage(
                url = item.bannerImg,
                fallbackText = item.title,          // letter tile when there's no image
                compact = true,                     // small icon, no text
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Line 1: title ............ time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = TimeFormatter.relative(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (unread) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Line 2: description ...... unread dot
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = item.description.ifBlank { "No description" },
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = if (item.description.isBlank()) FontStyle.Italic else FontStyle.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (unread) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .padding(top = 6.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            }
        }
    }
}

// ---------- Empty states ----------

@Composable
private fun EmptyInbox(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon inside a soft indigo circle
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.size(20.dp))
        Text(
            "No notifications yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "Push notifications you receive will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun NoResults(query: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = "No results for \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ---------- Previews ----------

private val now = System.currentTimeMillis()

private val sampleItems = listOf(
    NotificationEntity(
        id = "n1",
        title = "Flash sale starts now",
        description = "Up to 50% off on selected items. Offer valid for the next 24 hours only.",
        bannerImg = "https://example.com/sale.jpg",
        timestamp = now - 5 * 60_000L,
        isRead = false
    ),
    NotificationEntity(
        id = "n2",
        title = "Your order has shipped",
        description = "",
        bannerImg = null,
        timestamp = now - 3 * 3_600_000L,
        isRead = false
    ),
    NotificationEntity(
        id = "n3",
        title = "Welcome to NotifyHub",
        description = "Thanks for installing. All your notifications will be saved here.",
        bannerImg = "https://example.com/welcome.jpg",
        timestamp = now - 2 * 86_400_000L,
        isRead = true
    )
)

@Preview(name = "List", showBackground = true)
@Composable
private fun InboxListPreview() {
    NotifyHubTheme {
        InboxScreen(InboxUiState(items = sampleItems, unreadCount = 2, isLoading = false), onOpenDetail = {})
    }
}

@Preview(name = "List · unread first", showBackground = true)
@Composable
private fun InboxUnreadFirstPreview() {
    NotifyHubTheme {
        InboxScreen(
            InboxUiState(items = sampleItems, unreadCount = 2, isLoading = false, sortOrder = SortOrder.UNREAD_FIRST),
            onOpenDetail = {}
        )
    }
}

@Preview(name = "Search · no results", showBackground = true)
@Composable
private fun InboxNoResultsPreview() {
    NotifyHubTheme {
        InboxScreen(InboxUiState(items = emptyList(), isLoading = false, query = "zzzz"), onOpenDetail = {})
    }
}

@Preview(name = "Empty", showBackground = true)
@Composable
private fun InboxEmptyPreview() {
    NotifyHubTheme {
        InboxScreen(InboxUiState(items = emptyList(), isLoading = false), onOpenDetail = {})
    }
}

@Preview(name = "Loading", showBackground = true)
@Composable
private fun InboxLoadingPreview() {
    NotifyHubTheme {
        InboxScreen(InboxUiState(), onOpenDetail = {})
    }
}