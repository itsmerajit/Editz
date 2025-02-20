package com.editz.ui.editor.tools.opacity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.tools.VideoToolControls

class OpacityTool : VideoToolControls {
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var opacity by remember { mutableFloatStateOf(1f) }
        
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Video Opacity",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Slider(
                value = opacity,
                onValueChange = { 
                    opacity = it
                    onValueChanged()
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = EditzColors.Purple,
                    activeTrackColor = EditzColors.Purple,
                    inactiveTrackColor = EditzColors.Surface
                )
            )
            
            Text(
                text = "${(opacity * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
        }
    }
} 