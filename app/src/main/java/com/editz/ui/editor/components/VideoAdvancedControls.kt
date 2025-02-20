package com.editz.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.model.VideoTool

@Composable
fun VideoAdvancedControls(
    selectedTool: VideoTool?,
    onToolSelected: (VideoTool) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier
    ) {
        items(VideoTool.values()) { tool ->
            ToolItem(
                tool = tool,
                isSelected = tool == selectedTool,
                onClick = { onToolSelected(tool) }
            )
        }
    }
}

@Composable
private fun ToolItem(
    tool: VideoTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) EditzColors.Purple.copy(alpha = 0.1f)
                else EditzColors.Surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
            .width(80.dp)
    ) {
        Icon(
            imageVector = when (tool) {
                VideoTool.STITCH -> Icons.Default.Link
                VideoTool.TRIM -> Icons.Default.ContentCut
                VideoTool.MASK -> Icons.Default.Masks
                VideoTool.OPACITY -> Icons.Default.Opacity
                VideoTool.REPLACE -> Icons.Default.SwapHoriz
                VideoTool.VOICE_EFFECT -> Icons.Default.RecordVoiceOver
                VideoTool.DUPLICATE -> Icons.Default.ContentCopy
                VideoTool.ROTATE -> Icons.Default.Rotate90DegreesCcw
                VideoTool.SPEED -> Icons.Default.Speed
                VideoTool.VOICE -> Icons.Default.VoiceChat
            },
            contentDescription = tool.name,
            tint = if (isSelected) EditzColors.Purple else EditzColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = tool.name.replace("_", " ").lowercase()
                .split(" ")
                .joinToString(" ") { it.capitalize() },
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextSecondary
        )
    }
}

// Tool-specific control composables
@Composable
private fun SpeedControls() {
    var speed by remember { mutableFloatStateOf(1f) }
    Column {
        Text("Playback Speed", color = EditzColors.TextPrimary)
        Slider(
            value = speed,
            onValueChange = { speed = it },
            valueRange = 0.25f..2f,
            steps = 7
        )
        Text("${speed}x", color = EditzColors.TextSecondary)
    }
}

@Composable
private fun VolumeControls() {
    var volume by remember { mutableFloatStateOf(1f) }
    Column {
        Text("Volume", color = EditzColors.TextPrimary)
        Slider(
            value = volume,
            onValueChange = { volume = it },
            valueRange = 0f..1f
        )
        Text("${(volume * 100).toInt()}%", color = EditzColors.TextSecondary)
    }
}

// Add other tool controls as needed
@Composable private fun RotateControls() { /* TODO */ }
@Composable private fun CropControls() { /* TODO */ }
@Composable private fun TextControls() { /* TODO */ }
@Composable private fun StickerControls() { /* TODO */ }
@Composable private fun TransitionControls() { /* TODO */ }
@Composable private fun MusicControls() { /* TODO */ }
@Composable private fun VoiceControls() { /* TODO */ }
@Composable private fun SplitControls() { /* TODO */ }
@Composable private fun MergeControls() { /* TODO */ }
@Composable private fun ExportControls() { /* TODO */ } 