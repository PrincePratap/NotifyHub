package com.cody.notifyhub.ui.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.ui.components.BannerImage
import com.cody.notifyhub.ui.theme.NotifyHubTheme
import com.cody.notifyhub.util.TimeFormatter

// ---------- Route: connects to the ViewModel ----------

@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(
        state = state,
        onBack = onBack,
        onDelete = { viewModel.delete(onDone = onBack) }   // go back only after the delete finishes
    )
}

// ---------- Screen: pure UI, previewable ----------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    state: DetailUiState,
    onBack: () -> Unit,
    onDelete: () -> Unit = {}
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { Text("Notification", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Delete only makes sense when there is something to delete
                    if (state is DetailUiState.Success) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state) {
                DetailUiState.Loading ->
                    CircularProgressIndicator(Modifier.align(Alignment.Center))

                DetailUiState.NotFound ->
                    NotFoundContent(onBack = onBack, modifier = Modifier.align(Alignment.Center))

                is DetailUiState.Success ->
                    DetailContent(state.item)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete notification?") },
            text = { Text("It will be removed from your inbox.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DetailContent(item: NotificationEntity) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Banner only if a URL exists; a broken URL shows the placeholder (handled in BannerImage)
        if (item.bannerImg != null) {
            BannerImage(
                url = item.bannerImg,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        // Content card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimePill(item.timestamp)

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // Body: long-press to select and copy
                SelectionContainer {
                    if (item.description.isBlank()) {
                        Text(
                            text = "No description",
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimePill(timestamp: Long) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            text = "${TimeFormatter.full(timestamp)}  •  ${TimeFormatter.relative(timestamp)}",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun NotFoundContent(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.size(20.dp))
        Text(
            "Notification not found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.size(4.dp))
        Text(
            text = "It may have been deleted.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.size(16.dp))
        OutlinedButton(onClick = onBack) { Text("Back to inbox") }
    }
}

// ---------- Previews ----------

private val sampleItem = NotificationEntity(
    id = "notification_101",
    title = "New Notification",
    description = "This is a sample notification for the assignment. " +
            "It has a longer description so you can check how the text wraps on the details screen.",
    bannerImg = "https://example.com/banner.jpg",   // won't load in preview → placeholder shows
    timestamp = 1791873000000,
    isRead = true
)

@Preview(name = "With banner", showBackground = true)
@Composable
private fun DetailWithBannerPreview() {
    NotifyHubTheme { DetailScreen(DetailUiState.Success(sampleItem), onBack = {}) }
}

@Preview(name = "No banner", showBackground = true)
@Composable
private fun DetailNoBannerPreview() {
    NotifyHubTheme { DetailScreen(DetailUiState.Success(sampleItem.copy(bannerImg = null)), onBack = {}) }
}

@Preview(name = "Empty description", showBackground = true)
@Composable
private fun DetailEmptyDescriptionPreview() {
    NotifyHubTheme { DetailScreen(DetailUiState.Success(sampleItem.copy(description = "")), onBack = {}) }
}

@Preview(name = "Not found", showBackground = true)
@Composable
private fun DetailNotFoundPreview() {
    NotifyHubTheme { DetailScreen(DetailUiState.NotFound, onBack = {}) }
}