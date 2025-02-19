package com.editz.ui.editor.components

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
import com.editz.ui.editor.model.VideoEffect
import androidx.compose.ui.graphics.Color

@Composable
fun VideoEffects(
    selectedEffect: VideoEffect,
    onEffectSelected: (VideoEffect) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Effects",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
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
            .width(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) EditzColors.Purple.copy(alpha = 0.2f)
                else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF1A1A1A))
        ) {
            // Effect preview placeholder
            when (effect) {
                VideoEffect.NONE -> {
                    Text(
                        text = "None",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.Center)
                    )
                }
                else -> {
                    // TODO: Add effect preview
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = effect.name.lowercase().capitalize(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) EditzColors.Purple else Color.White
        )
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 