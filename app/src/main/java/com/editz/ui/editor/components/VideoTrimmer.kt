package com.editz.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.editz.theme.EditzColors
import kotlin.math.roundToLong
import kotlin.math.max
import kotlin.math.min

private const val MIN_TRIM_DURATION = 1000L // 1 second
private const val FRAME_DURATION = 33L // ~30fps

@Composable
fun VideoTrimmer(
    duration: Long,
    currentPosition: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onStartMsChange: (Long) -> Unit,
    onEndMsChange: (Long) -> Unit,
    onCurrentPositionChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val timelineWidth = 360.dp
    val timelineWidthPx = with(density) { timelineWidth.toPx() }
    val durationFloat = duration.toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Trim Video",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        RangeSlider(
            value = trimStartMs.toFloat()..trimEndMs.toFloat(),
            onValueChange = { range ->
                onStartMsChange(range.start.toLong())
                onEndMsChange(range.endInclusive.toLong())
            },
            valueRange = 0f..duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple,
                inactiveTrackColor = EditzColors.Surface
            )
        )
        
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onCurrentPositionChange(it.toLong()) },
            valueRange = trimStartMs.toFloat()..trimEndMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple,
                inactiveTrackColor = EditzColors.Surface
            )
        )
        
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = formatDuration(trimStartMs),
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
            Text(
                text = formatDuration(trimEndMs),
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
        }
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