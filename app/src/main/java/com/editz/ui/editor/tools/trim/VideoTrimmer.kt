package com.editz.ui.editor.tools.trim

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.components.VideoTimeline
import com.editz.utils.ThumbnailGenerator
import kotlinx.coroutines.launch
import kotlin.math.roundToLong

@Composable
fun VideoTrimmer(
    uri: Uri,
    duration: Long,
    currentPosition: Long,
    onCurrentPositionChange: (Long) -> Unit,
    onTrimRangeChange: (start: Long, end: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnailGenerator = remember { ThumbnailGenerator(context) }
    var thumbnails by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var trimStartMs by remember { mutableStateOf(0L) }
    var trimEndMs by remember { mutableStateOf(duration) }
    
    LaunchedEffect(uri) {
        thumbnails = thumbnailGenerator.generateThumbnailStrip(
            uri = uri,
            duration = duration,
            numThumbnails = 8
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            thumbnailGenerator.clearCache()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VideoTimeline(
            thumbnails = thumbnails,
            duration = duration,
            currentPosition = currentPosition,
            trimStartMs = trimStartMs,
            trimEndMs = trimEndMs,
            onStartMsChange = { newStart ->
                trimStartMs = newStart
                onTrimRangeChange(newStart, trimEndMs)
            },
            onEndMsChange = { newEnd ->
                trimEndMs = newEnd
                onTrimRangeChange(trimStartMs, newEnd)
            },
            onCurrentPositionChange = onCurrentPositionChange
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Trim Duration: ${formatDuration(trimEndMs - trimStartMs)}",
            style = MaterialTheme.typography.bodyMedium,
            color = EditzColors.TextPrimary
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = durationMs / (1000 * 60 * 60)
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
} 