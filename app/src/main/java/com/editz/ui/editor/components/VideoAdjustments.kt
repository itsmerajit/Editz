package com.editz.ui.editor.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

data class VideoAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f
)

@Composable
fun VideoAdjustments(
    adjustments: VideoAdjustments,
    onAdjustmentsChanged: (VideoAdjustments) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Adjustments",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AdjustmentSlider(
            label = "Brightness",
            value = adjustments.brightness,
            onValueChange = { 
                onAdjustmentsChanged(adjustments.copy(brightness = it))
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdjustmentSlider(
            label = "Contrast",
            value = adjustments.contrast,
            onValueChange = { 
                onAdjustmentsChanged(adjustments.copy(contrast = it))
            }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        AdjustmentSlider(
            label = "Saturation",
            value = adjustments.saturation,
            onValueChange = { 
                onAdjustmentsChanged(adjustments.copy(saturation = it))
            }
        )
    }
}

@Composable
private fun AdjustmentSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
            
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = EditzColors.TextSecondary
            )
        }
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = -1f..1f,
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple,
                inactiveTrackColor = EditzColors.Surface
            )
        )
    }
} 