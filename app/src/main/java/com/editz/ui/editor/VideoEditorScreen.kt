package com.editz.ui.editor

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.utils.VideoDetails
import androidx.hilt.navigation.compose.hiltViewModel
import com.editz.ui.preview.VideoPreviewScreen

@Composable
fun VideoEditorScreen(
    videoDetails: VideoDetails,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: VideoEditorViewModel = hiltViewModel()
) {
    var selectedTool by remember { mutableStateOf<EditorTool?>(null) }
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(videoDetails) {
        viewModel.initializeEditor(videoDetails)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
    ) {
        // Top Bar
        TopAppBar(
            title = { Text("Edit Video", color = EditzColors.TextPrimary) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = EditzColors.TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = EditzColors.Surface
            )
        )

        when (uiState) {
            is VideoEditorUiState.Initial -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = EditzColors.Purple
                )
            }
            is VideoEditorUiState.Editing -> {
                val editingState = uiState as VideoEditorUiState.Editing
                
                // Video Preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    VideoPreviewScreen(
                        videoDetails = editingState.videoDetails,
                        volume = editingState.volume,
                        speed = editingState.speed
                    )
                }

                // Tools Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(editorTools) { tool ->
                        EditorToolItem(
                            tool = tool,
                            isSelected = selectedTool == tool,
                            onClick = { selectedTool = tool }
                        )
                    }
                }

                // Tool Options
                selectedTool?.let { tool ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = EditzColors.Surface
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            when (tool) {
                                EditorTool.Trim -> TrimControls(
                                    duration = editingState.videoDetails.duration,
                                    startMs = editingState.trimStartMs,
                                    endMs = editingState.trimEndMs,
                                    onTrimPointsChanged = viewModel::updateTrimPoints
                                )
                                EditorTool.Volume -> VolumeControls(
                                    volume = editingState.volume,
                                    onVolumeChanged = viewModel::updateVolume
                                )
                                EditorTool.Speed -> SpeedControls(
                                    speed = editingState.speed,
                                    onSpeedChanged = viewModel::updateSpeed
                                )
                                EditorTool.Filter -> FilterControls(
                                    currentFilter = editingState.filter,
                                    onFilterChanged = viewModel::updateFilter
                                )
                            }
                        }
                    }
                }
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
        // TODO: Implement trim slider
        Text("Trim Controls Coming Soon", color = EditzColors.TextSecondary)
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