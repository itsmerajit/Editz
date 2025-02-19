package com.editz.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

enum class VideoEffect(
    val displayName: String,
    val icon: ImageVector,
    val intensity: Float = 1f
) {
    NONE("None", Icons.Default.FilterNone),
    BLUR("Blur", Icons.Default.BlurOn, 0.5f),
    MIRROR("Mirror", Icons.Default.Flip, 1f),
    GLITCH("Glitch", Icons.Default.BrokenImage, 0.3f),
    ZOOM("Zoom", Icons.Default.ZoomIn, 0.4f),
    PIXELATE("Pixelate", Icons.Default.Grid4x4, 0.6f),
    VIGNETTE("Vignette", Icons.Default.RadioButtonUnchecked, 0.4f)
}

@Composable
fun VideoEffects(
    selectedEffect: VideoEffect,
    onEffectSelected: (VideoEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Effects",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VideoEffect.values()) { effect ->
                EffectItem(
                    effect = effect,
                    isSelected = effect == selectedEffect,
                    onClick = { onEffectSelected(effect) }
                )
            }
        }
        
        // Intensity Slider if effect is selected
        if (selectedEffect != VideoEffect.NONE) {
            Spacer(modifier = Modifier.height(16.dp))
            IntensitySlider(
                value = selectedEffect.intensity,
                onValueChange = { /* TODO: Handle intensity change */ }
            )
        }
    }
}

@Composable
private fun EffectItem(
    effect: VideoEffect,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) EditzColors.Purple.copy(alpha = 0.1f)
                else EditzColors.Surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
            .width(80.dp)
    ) {
        Icon(
            imageVector = effect.icon,
            contentDescription = effect.displayName,
            tint = if (isSelected) EditzColors.Purple else EditzColors.TextSecondary,
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = effect.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextSecondary
        )
    }
}

@Composable
private fun IntensitySlider(
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
                text = "Intensity",
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
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple,
                inactiveTrackColor = EditzColors.Surface
            )
        )
    }
} 