package com.cody.notifyhub.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.R
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.cody.notifyhub.ui.inbox.DetailViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cody.notifyhub.data.local.NotificationEntity
import com.cody.notifyhub.ui.components.BannerImage
import com.cody.notifyhub.ui.inbox.DetailUiState
import com.cody.notifyhub.util.TimeFormatter


@Composable
fun DetailRoute(
    onBack: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(state = state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(state: DetailUiState, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notification") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {                                   // the compiler forces all 3 cases
                DetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                DetailUiState.NotFound -> Text("This notification no longer exists.", Modifier.align(Alignment.Center))
                is DetailUiState.Success -> DetailContent(state.item)
            }
        }
    }
}

@Composable
private fun DetailContent(item: NotificationEntity) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if (item.bannerImg != null) {
            BannerImage(item.bannerImg, Modifier.fillMaxWidth().aspectRatio(16f / 9f))
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(item.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                TimeFormatter.full(item.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}