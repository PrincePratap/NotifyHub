package com.cody.notifyhub.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.cody.notifyhub.R

/**
 * Shows a banner image and never blocks the surrounding text.
 *
 * - url == null  → letter tile (first letter of [fallbackText])
 * - loading      → pulsing tile
 * - error        → broken-image icon (+ text when not [compact])
 * - success      → image with crossfade
 */
@Composable
fun BannerImage(
    url: String?,
    modifier: Modifier = Modifier,
    fallbackText: String? = null,   // usually the notification title
    compact: Boolean = false        // true for small list thumbnails
) {
    if (url.isNullOrBlank()) {
        LetterTile(text = fallbackText, modifier = modifier)
        return
    }

    var state by remember(url) { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

    val request = ImageRequest.Builder(LocalPlatformContext.current)
        .data(url)
        .crossfade(true)
        .build()

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            onState = { state = it }
        )

        when (state) {
            is AsyncImagePainter.State.Loading -> LoadingPulse(Modifier.matchParentSize())
            is AsyncImagePainter.State.Error -> ErrorContent(compact = compact)
            else -> Unit             // Success or Empty: just the image
        }
    }
}

@Composable
private fun LetterTile(text: String?, modifier: Modifier) {
    val letter = text?.trim()?.firstOrNull()?.uppercaseChar()?.toString()

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (letter != null) {
            Text(
                text = letter,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_notification),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LoadingPulse(modifier: Modifier) {
    val transition = rememberInfiniteTransition(label = "imagePulse")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 800), RepeatMode.Reverse),
        label = "alpha"
    )
    Box(modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)))
}

@Composable
private fun ErrorContent(compact: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_broken_image),
            contentDescription = "Image unavailable",
            modifier = Modifier.size(if (compact) 24.dp else 40.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!compact) {
            Text(
                text = "Image unavailable",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}