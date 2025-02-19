package com.editz.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.utils.VideoDetails
import androidx.hilt.navigation.compose.hiltViewModel
import com.editz.ui.preview.VideoPreviewScreen
import com.editz.ui.editor.components.VideoTrimSlider
import com.editz.ui.editor.components.VideoFilters
import com.editz.ui.editor.components.VideoAdjustments
import com.editz.ui.editor.components.VideoFilter
import com.editz.ui.editor.components.VideoEffects
import com.editz.ui.editor.components.VideoEffect

@Composable
fun VideoEditorScreen(
    videoDetails: VideoDetails,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoEditorViewModel = hiltViewModel()
) {
    val currentFilter by viewModel.currentFilter.collectAsState()
    val adjustments by viewModel.adjustments.collectAsState()
    val effectState by viewModel.effectState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeEditor(videoDetails)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
    ) {
        // Top Bar with back button
        TopAppBar(
            title = { Text("Edit Video") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = EditzColors.Surface,
                titleContentColor = EditzColors.TextPrimary,
                navigationIconContentColor = EditzColors.TextPrimary
            )
        )

        // Video Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
        ) {
            VideoPreviewScreen(
                videoDetails = videoDetails,
                volume = 1f,
                speed = 1f,
                startMs = 0,
                endMs = videoDetails.duration
            )
        }

        // Editing Tools
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            // Filters Section
            VideoFilters(
                selectedFilter = currentFilter,
                onFilterSelected = viewModel::updateFilter
            )
            
            Divider(color = EditzColors.Surface)
            
            // Effects Section
            VideoEffects(
                selectedEffect = effectState.effect,
                onEffectSelected = viewModel::updateEffect
            )
            
            Divider(color = EditzColors.Surface)
            
            // Adjustments Section
            VideoAdjustments(
                adjustments = adjustments,
                onAdjustmentsChanged = viewModel::updateAdjustments
            )
            
            Divider(color = EditzColors.Surface)
            
            // Reset Button
            TextButton(
                onClick = viewModel::resetEdits,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Reset All",
                    color = EditzColors.Purple
                )
            }
        }
    }
}

@Composable
private fun EditorToolItem(
    tool: EditorTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EditzColors.Purple else EditzColors.Surface
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                tint = if (isSelected) Color.White else EditzColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tool.title,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) Color.White else EditzColors.TextPrimary
            )
        }
    }
}

@Composable
private fun TrimControls(
    duration: Long,
    startMs: Long,
    endMs: Long,
    onTrimPointsChanged: (Long, Long) -> Unit
) {
    Column {
        Text("Trim Video", color = EditzColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        VideoTrimSlider(
            duration = duration,
            startMs = startMs,
            endMs = endMs.takeIf { it > 0 } ?: duration,
            onStartMsChange = { newStartMs ->
                onTrimPointsChanged(newStartMs, endMs.takeIf { it > 0 } ?: duration)
            },
            onEndMsChange = { newEndMs ->
                onTrimPointsChanged(startMs, newEndMs)
            }
        )
        Text(
            text = "Duration: ${formatDuration(endMs - startMs)}",
            style = MaterialTheme.typography.bodySmall,
            color = EditzColors.TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun VolumeControls(
    volume: Float,
    onVolumeChanged: (Float) -> Unit
) {
    Column {
        Text("Volume", color = EditzColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = volume,
            onValueChange = onVolumeChanged,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple
            )
        )
        Text(
            text = "${(volume * 100).toInt()}%",
            color = EditzColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun SpeedControls(
    speed: Float,
    onSpeedChanged: (Float) -> Unit
) {
    Column {
        Text("Playback Speed", color = EditzColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = speed,
            onValueChange = onSpeedChanged,
            valueRange = 0.5f..2f,
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple
            )
        )
        Text(
            text = "${speed}x",
            color = EditzColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun FilterControls(
    currentFilter: String?,
    onFilterChanged: (String?) -> Unit
) {
    Column {
        Text("Video Filters", color = EditzColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        // TODO: Implement filter options
        Text("Filters Coming Soon", color = EditzColors.TextSecondary)
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

enum class EditorTool(val title: String, val icon: ImageVector) {
    Trim("Trim", Icons.Default.ContentCut),
    Volume("Volume", Icons.Default.VolumeUp),
    Speed("Speed", Icons.Default.Speed),
    Filter("Filter", Icons.Default.FilterAlt)
}

private val editorTools = listOf(
    EditorTool.Trim,
    EditorTool.Volume,
    EditorTool.Speed,
    EditorTool.Filter
) 