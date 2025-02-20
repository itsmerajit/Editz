package com.editz.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import com.editz.ui.editor.tools.ToolFactory

@Composable
fun VideoEditorScreen(
    videoDetails: VideoDetails,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoEditorViewModel = hiltViewModel()
) {
    var selectedTool by remember { mutableStateOf<VideoTool?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    
    // Initialize editor when video details are available
    LaunchedEffect(videoDetails) {
        viewModel.initializeEditor(videoDetails)
    }
    
    // Collect states
    val volume by viewModel.volume.collectAsState()
    val speed by viewModel.speed.collectAsState()
    val trimStartMs by viewModel.trimStartMs.collectAsState()
    val trimEndMs by viewModel.trimEndMs.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isPlaying = false
                    onBack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = { /* TODO: Show Help */ }) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Help",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "Export",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .clickable { viewModel.saveChanges() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Video Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = videoDetails.name,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Duration: ${formatDuration(videoDetails.duration)}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Resolution: ${videoDetails.width}x${videoDetails.height}",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Video Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                VideoPreviewScreen(
                    videoDetails = videoDetails,
                    volume = volume,
                    speed = speed,
                    startMs = trimStartMs,
                    endMs = trimEndMs,
                    isPlaying = isPlaying,
                    onPlayPause = { 
                        isPlaying = !isPlaying
                    },
                    onSeek = { position ->
                        currentPosition = position
                        viewModel.updatePosition(position)
                    }
                )
                
                // Center Play Button
                IconButton(
                    onClick = { isPlaying = !isPlaying },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Progress Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(currentPosition),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = formatDuration(videoDetails.duration),
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                Slider(
                    value = currentPosition.toFloat(),
                    onValueChange = { 
                        currentPosition = it.toLong()
                        viewModel.updatePosition(it.toLong())
                        isPlaying = false
                    },
                    valueRange = 0f..videoDetails.duration.toFloat(),
                    colors = SliderDefaults.colors(
                        thumbColor = EditzColors.Purple,
                        activeTrackColor = EditzColors.Purple,
                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            // Tools Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A))
                    .padding(vertical = 16.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                VideoTool.values().forEach { tool ->
                    ToolButton(
                        tool = tool,
                        isSelected = tool == selectedTool,
                        onClick = { 
                            selectedTool = if (selectedTool == tool) null else tool
                            isPlaying = false
                        }
                    )
                }
            }

            // Tool-specific controls
            selectedTool?.let { tool ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1A1A1A))
                        .padding(vertical = 16.dp)
                ) {
                    when (tool) {
                        VideoTool.TRIM -> VideoTrimmer(
                            duration = videoDetails.duration,
                            currentPosition = currentPosition,
                            trimStartMs = trimStartMs,
                            trimEndMs = trimEndMs,
                            onStartMsChange = viewModel::updateTrimStart,
                            onEndMsChange = viewModel::updateTrimEnd,
                            onCurrentPositionChange = { position ->
                                currentPosition = position
                                viewModel.updatePosition(position)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        else -> ToolFactory.createTool(tool).Content(
                            modifier = Modifier.fillMaxWidth(),
                            onValueChanged = {
                                // Handle tool-specific changes through ViewModel
                                when (tool) {
                                    VideoTool.STITCH -> { /* Handle stitch */ }
                                    VideoTool.MASK -> { /* Handle mask */ }
                                    VideoTool.OPACITY -> { /* Handle opacity */ }
                                    VideoTool.REPLACE -> { /* Handle replace */ }
                                    VideoTool.VOICE_EFFECT -> { /* Handle voice */ }
                                    VideoTool.DUPLICATE -> { /* Handle duplicate */ }
                                    VideoTool.ROTATE -> { /* Handle rotate */ }
                                    VideoTool.SPEED -> { /* Handle speed */ }
                                    VideoTool.TRIM -> { /* Handle trim */ }
                                    VideoTool.VOICE -> { /* Handle voice */ }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolButton(
    tool: VideoTool,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
            .padding(8.dp)
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
            contentDescription = when (tool) {
                VideoTool.STITCH -> "Stitch videos together"
                VideoTool.TRIM -> "Trim video length"
                VideoTool.MASK -> "Apply video mask"
                VideoTool.OPACITY -> "Adjust video opacity"
                VideoTool.REPLACE -> "Replace video segment"
                VideoTool.VOICE_EFFECT -> "Add voice effects"
                VideoTool.DUPLICATE -> "Duplicate video segment"
                VideoTool.ROTATE -> "Rotate video"
                VideoTool.SPEED -> "Adjust playback speed"
                VideoTool.VOICE -> "Voice recording"
            },
            tint = if (isSelected) EditzColors.Purple else Color.White,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = when (tool) {
                VideoTool.STITCH -> "Stitch"
                VideoTool.TRIM -> "Trim"
                VideoTool.MASK -> "Mask"
                VideoTool.OPACITY -> "Opacity"
                VideoTool.REPLACE -> "Replace"
                VideoTool.VOICE_EFFECT -> "Voice Effect"
                VideoTool.DUPLICATE -> "Duplicate"
                VideoTool.ROTATE -> "Rotate"
                VideoTool.SPEED -> "Speed"
                VideoTool.VOICE -> "Voice"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) EditzColors.Purple else Color.White,
            maxLines = 1
        )
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    return String.format("%02d:%02d", minutes, seconds)
} 