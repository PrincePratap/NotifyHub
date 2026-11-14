package com.cody.notifyhub.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

@Composable
fun BannerImage(url: String?, modifier: Modifier = Modifier) {
    val placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    AsyncImage(
        model = url,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        placeholder = placeholder,   // while loading
        error = placeholder,         // bad URL, 404, no internet
        fallback = placeholder       // url is null
    )
}