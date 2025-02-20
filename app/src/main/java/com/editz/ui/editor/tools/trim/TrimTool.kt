package com.editz.ui.editor.tools.trim

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.tools.VideoToolControls
import com.editz.ui.editor.components.VideoTrimmer

class TrimTool : VideoToolControls {
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var trimStartMs by remember { mutableStateOf(0L) }
        var trimEndMs by remember { mutableStateOf(0L) }
        var currentPosition by remember { mutableStateOf(0L) }
        var duration by remember { mutableStateOf(0L) }
        
        // TODO: Get actual video duration from VideoProcessor
        duration = 60000L // 1 minute for testing
        
        if (trimEndMs == 0L) {
            trimEndMs = duration
        }
        
        VideoTrimmer(
            duration = duration,
            currentPosition = currentPosition,
            trimStartMs = trimStartMs,
            trimEndMs = trimEndMs,
            onStartMsChange = { 
                trimStartMs = it
                onValueChanged()
            },
            onEndMsChange = { 
                trimEndMs = it
                onValueChanged()
            },
            onCurrentPositionChange = { 
                currentPosition = it
                onValueChanged()
            },
            modifier = modifier
        )
    }
    
    private fun formatDuration(durationMs: Long): String {
        val seconds = (durationMs / 1000) % 60
        val minutes = (durationMs / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
} 