package com.editz.ui.editor.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

class OpacityTool : VideoToolControls {
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var opacity by remember { mutableFloatStateOf(1f) }
        
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opacity",
                    style = MaterialTheme.typography.titleMedium,
                    color = EditzColors.TextPrimary
                )
                
                Text(
                    text = "${(opacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditzColors.TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Preset opacity values
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OpacityPreset(value = 0.25f, opacity = opacity) {
                    opacity = it
                    onValueChanged()
                }
                OpacityPreset(value = 0.5f, opacity = opacity) {
                    opacity = it
                    onValueChanged()
                }
                OpacityPreset(value = 0.75f, opacity = opacity) {
                    opacity = it
                    onValueChanged()
                }
                OpacityPreset(value = 1f, opacity = opacity) {
                    opacity = it
                    onValueChanged()
                }
            }
        }
    }
}

@Composable
private fun OpacityPreset(
    value: Float,
    opacity: Float,
    onSelect: (Float) -> Unit
) {
    val isSelected = opacity == value
    
    Button(
        onClick = { onSelect(value) },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) EditzColors.Purple 
                           else EditzColors.Surface
        ),
        modifier = Modifier.size(48.dp)
    ) {
        Text(
            text = "${(value * 100).toInt()}%",
            color = if (isSelected) EditzColors.TextPrimary 
                    else EditzColors.TextSecondary,
            style = MaterialTheme.typography.bodySmall
        )
    }
} 