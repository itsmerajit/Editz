package com.editz.ui.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

@Composable
fun VideoTrimSlider(
    duration: Long,
    startMs: Long,
    endMs: Long,
    onStartMsChange: (Long) -> Unit,
    onEndMsChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var sliderWidth by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Column(modifier = modifier) {
        // Time indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(startMs),
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
            Text(
                text = formatDuration(endMs),
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Trim slider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(EditzColors.Surface)
                .onSizeChanged {
                    sliderWidth = it.width.toFloat()
                }
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .align(Alignment.Center)
                    .background(Color.Gray.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(density) { ((endMs - startMs) / duration.toFloat() * sliderWidth).toDp() })
                        .offset(
                            x = with(density) { (startMs / duration.toFloat() * sliderWidth).toDp() }
                        )
                        .background(EditzColors.Purple)
                )
            }

            // Start handle
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (startMs / duration.toFloat() * sliderWidth).toDp() }
                    )
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EditzColors.Purple)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            val newX = (startMs / duration.toFloat() * sliderWidth) + dragAmount
                            val newStartMs = (newX / sliderWidth * duration).toLong()
                                .coerceIn(0, endMs - 1000)
                            onStartMsChange(newStartMs)
                        }
                    }
            )

            // End handle
            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { (endMs / duration.toFloat() * sliderWidth).toDp() }
                    )
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(EditzColors.Purple)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            val newX = (endMs / duration.toFloat() * sliderWidth) + dragAmount
                            val newEndMs = (newX / sliderWidth * duration).toLong()
                                .coerceIn(startMs + 1000, duration)
                            onEndMsChange(newEndMs)
                        }
                    }
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
} 