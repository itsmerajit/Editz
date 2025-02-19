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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.data.VideoDetails
import androidx.hilt.navigation.compose.hiltViewModel
import com.editz.ui.preview.VideoPreviewScreen
import com.editz.ui.editor.components.VideoTrimSlider
import com.editz.ui.editor.components.VideoFilters
import com.editz.ui.editor.components.VideoAdjustments
import com.editz.ui.editor.model.VideoFilter
import com.editz.ui.editor.components.VideoEffects
import com.editz.ui.editor.model.VideoEffect
import com.editz.ui.editor.components.VideoTrimmer
import com.editz.ui.editor.components.VideoAdvancedControls
import com.editz.ui.editor.model.VideoTool
import androidx.compose.foundation.clickable

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
    val currentPosition by viewModel.currentPosition.collectAsState()
    val trimStartMs by viewModel.trimStartMs.collectAsState()
    val trimEndMs by viewModel.trimEndMs.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val processingError by viewModel.processingError.collectAsState()
    
    var selectedTool by remember { mutableStateOf<VideoTool?>(null) }

    LaunchedEffect(Unit) {
        viewModel.initializeEditor(videoDetails)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
    ) {
        // Main Content
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Professional Top Bar
            TopAppBar(
                title = { 
                    Text(
                        text = "Edit Video",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Export action
                    IconButton(
                        onClick = viewModel::saveChanges
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Export"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EditzColors.Surface,
                    titleContentColor = EditzColors.TextPrimary
                )
            )

            // Video Preview Section with aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .background(EditzColors.Background)
            ) {
                VideoPreviewScreen(
                    videoDetails = videoDetails,
                    startMs = trimStartMs,
                    endMs = trimEndMs,
                    onSeek = viewModel::updatePosition
                )
            }

            // Timeline/Trimmer with professional look
            VideoTrimmer(
                duration = videoDetails.duration,
                currentPosition = currentPosition,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs,
                onStartMsChange = viewModel::updateTrimStart,
                onEndMsChange = viewModel::updateTrimEnd,
                onCurrentPositionChange = viewModel::updatePosition,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp)
            )

            // Quick Action Tools
            QuickActionTools(
                selectedTool = selectedTool,
                onToolSelected = { tool -> selectedTool = tool }
            )

            // Editing Workspace
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .background(EditzColors.Surface)
            ) {
                when (selectedTool) {
                    VideoTool.FILTER -> VideoFilters(
                        selectedFilter = currentFilter,
                        onFilterSelected = viewModel::updateFilter
                    )
                    VideoTool.EFFECT -> VideoEffects(
                        selectedEffect = effectState.effect,
                        onEffectSelected = viewModel::updateEffect
                    )
                    VideoTool.ADJUST -> VideoAdjustments(
                        adjustments = adjustments,
                        onAdjustmentsChanged = viewModel::updateAdjustments
                    )
                    else -> {
                        // Show welcome message or tips
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Select a tool to start editing",
                                style = MaterialTheme.typography.bodyLarge,
                                color = EditzColors.TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Processing Overlay
        if (isProcessing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EditzColors.Background.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = EditzColors.Purple)
                    Text(
                        text = "Processing video...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = EditzColors.TextPrimary
                    )
                }
            }
        }

        // Error Dialog
        processingError?.let { error ->
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Error") },
                text = { Text(error) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun QuickActionTools(
    selectedTool: VideoTool?,
    onToolSelected: (VideoTool) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(EditzColors.Surface)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        VideoTool.values().forEach { tool ->
            QuickActionButton(
                tool = tool,
                isSelected = tool == selectedTool,
                onClick = { onToolSelected(tool) }
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    tool: VideoTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        val icon = when (tool) {
            VideoTool.TRIM -> Icons.Default.ContentCut
            VideoTool.FILTER -> Icons.Default.FilterAlt
            VideoTool.EFFECT -> Icons.Default.AutoFixHigh
            VideoTool.ADJUST -> Icons.Default.Tune
            VideoTool.EXPORT -> Icons.Default.Save
        }

        Icon(
            imageVector = icon,
            contentDescription = tool.name,
            tint = if (isSelected) EditzColors.Purple else EditzColors.TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = tool.name,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextSecondary
        )
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