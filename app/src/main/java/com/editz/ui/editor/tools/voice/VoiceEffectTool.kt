package com.editz.ui.editor.tools.voice

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.tools.VideoToolControls

class VoiceEffectTool : VideoToolControls {
    private val effects = listOf("Normal", "Deep", "High", "Robot", "Echo")
    
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var selectedEffect by remember { mutableStateOf(effects.first()) }
        var intensity by remember { mutableFloatStateOf(0.5f) }
        
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Voice Effects",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Effect selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                effects.forEach { effect ->
                    Button(
                        onClick = {
                            selectedEffect = effect
                            onValueChanged()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedEffect == effect) 
                                EditzColors.Purple else EditzColors.Surface
                        )
                    ) {
                        Text(
                            text = effect,
                            color = if (selectedEffect == effect) 
                                EditzColors.TextPrimary else EditzColors.TextSecondary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Intensity slider
            Text(
                text = "Effect Intensity",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
            
            Slider(
                value = intensity,
                onValueChange = { 
                    intensity = it
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
                text = "${(intensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
        }
    }
} 