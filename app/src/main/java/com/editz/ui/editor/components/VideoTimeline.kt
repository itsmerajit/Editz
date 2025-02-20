package com.editz.ui.editor.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

@Composable
fun VideoTimeline(
    thumbnails: List<Bitmap>,
    duration: Long,
    currentPosition: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    onStartMsChange: (Long) -> Unit,
    onEndMsChange: (Long) -> Unit,
    onCurrentPositionChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Thumbnails strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EditzColors.Surface)
        ) {
            thumbnails.forEachIndexed { index, bitmap ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Frame $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Trim range slider
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Current position slider
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onCurrentPositionChange(it.toLong()) },
            valueRange = trimStartMs.toFloat()..trimEndMs.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple.copy(alpha = 0.5f),
                inactiveTrackColor = EditzColors.Surface
            )
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Current: ${formatDuration(currentPosition)}",
            style = MaterialTheme.typography.bodySmall,
            color = EditzColors.TextSecondary
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