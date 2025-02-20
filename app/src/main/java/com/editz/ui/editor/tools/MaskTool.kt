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

class MaskTool : VideoToolControls {
    private val masks = listOf(
        "Circle", "Square", "Heart", "Star",
        "Custom", "Gradient", "Blur"
    )

    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        var selectedMask by remember { mutableStateOf<String?>(null) }
        var maskIntensity by remember { mutableFloatStateOf(0.5f) }

        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Text(
                text = "Mask Effects",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(masks) { mask ->
                    MaskItem(
                        name = mask,
                        isSelected = mask == selectedMask,
                        onClick = {
                            selectedMask = mask
                            onValueChanged()
                        }
                    )
                }
            }
            
            if (selectedMask != null) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Intensity",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditzColors.TextSecondary
                )
                
                Slider(
                    value = maskIntensity,
                    onValueChange = { 
                        maskIntensity = it
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
                    text = "${(maskIntensity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = EditzColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun MaskItem(
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
                .background(EditzColors.Surface)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextPrimary
        )
    }
} 