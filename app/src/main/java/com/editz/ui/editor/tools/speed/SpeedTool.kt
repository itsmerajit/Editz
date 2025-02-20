package com.editz.ui.editor.tools.speed

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.tools.VideoToolControls

class SpeedTool : VideoToolControls {
    private val speedOptions = listOf(0.25f, 0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
    
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var selectedSpeed by remember { mutableFloatStateOf(1f) }
        
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Playback Speed",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Speed slider
            Slider(
                value = selectedSpeed,
                onValueChange = { 
                    selectedSpeed = it
                    onValueChanged()
                },
                valueRange = 0.25f..2f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = EditzColors.Purple,
                    activeTrackColor = EditzColors.Purple,
                    inactiveTrackColor = EditzColors.Surface
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${selectedSpeed}x",
                style = MaterialTheme.typography.bodyLarge,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Speed presets
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                speedOptions.forEach { speed ->
                    SpeedPresetButton(
                        speed = speed,
                        isSelected = selectedSpeed == speed,
                        onClick = {
                            selectedSpeed = speed
                            onValueChanged()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedPresetButton(
    speed: Float,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) EditzColors.Purple 
                           else EditzColors.Surface
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Text(
            text = "${speed}x",
            color = if (isSelected) EditzColors.TextPrimary 
                    else EditzColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
} 