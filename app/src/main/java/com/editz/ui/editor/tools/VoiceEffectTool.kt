package com.editz.ui.editor.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

class VoiceEffectTool : VideoToolControls {
    private val voiceEffects = listOf(
        "Normal", "Deep", "High", "Robot",
        "Echo", "Reverb", "Chorus"
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var selectedEffect by remember { mutableStateOf(voiceEffects.first()) }
        var effectIntensity by remember { mutableFloatStateOf(0.5f) }
        var pitch by remember { mutableFloatStateOf(1f) }
        
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Text(
                text = "Voice Effects",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(voiceEffects) { effect ->
                    EffectItem(
                        name = effect,
                        isSelected = effect == selectedEffect,
                        onClick = {
                            selectedEffect = effect
                            onValueChanged()
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Effect Intensity
            Text(
                text = "Effect Intensity",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
            
            Slider(
                value = effectIntensity,
                onValueChange = { 
                    effectIntensity = it
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
                text = "${(effectIntensity * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Pitch Control
            Text(
                text = "Pitch",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
            
            Slider(
                value = pitch,
                onValueChange = { 
                    pitch = it
                    onValueChanged()
                },
                valueRange = 0.5f..2f,
                colors = SliderDefaults.colors(
                    thumbColor = EditzColors.Purple,
                    activeTrackColor = EditzColors.Purple,
                    inactiveTrackColor = EditzColors.Surface
                )
            )
            
            Text(
                text = "${pitch}x",
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
        }
    }
}

@Composable
private fun EffectItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) EditzColors.Purple.copy(alpha = 0.1f)
                else EditzColors.Surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EditzColors.Surface),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎤",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextPrimary
        )
    }
} 