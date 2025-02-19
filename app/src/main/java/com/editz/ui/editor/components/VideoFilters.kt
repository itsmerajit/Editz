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
import com.editz.ui.editor.model.VideoFilter

@Composable
fun VideoFilters(
    selectedFilter: VideoFilter,
    onFilterSelected: (VideoFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            if (selectedFilter != VideoFilter.ORIGINAL) {
                TextButton(
                    onClick = { onFilterSelected(VideoFilter.ORIGINAL) }
                ) {
                    Text(
                        text = "Reset",
                        color = EditzColors.Purple
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Filter Grid
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(VideoFilter.values()) { filter ->
                FilterItem(
                    filter = filter,
                    isSelected = filter == selectedFilter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }
        
        if (selectedFilter != VideoFilter.ORIGINAL) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Filter Intensity Slider
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
                        text = "${(selectedFilter.intensity * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = EditzColors.TextSecondary
                    )
                }
                
                Slider(
                    value = selectedFilter.intensity,
                    onValueChange = { /* TODO: Implement intensity change */ },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = EditzColors.Purple,
                        activeTrackColor = EditzColors.Purple,
                        inactiveTrackColor = EditzColors.Surface
                    )
                )
            }
        }
    }
}

@Composable
private fun FilterItem(
    filter: VideoFilter,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) EditzColors.Purple.copy(alpha = 0.1f)
                else EditzColors.Surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        // Filter Preview
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EditzColors.Surface)
        ) {
            // TODO: Add actual filter preview with thumbnail
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = filter.displayName,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextPrimary
        )
        
        if (filter != VideoFilter.ORIGINAL) {
            Text(
                text = "${(filter.intensity * 100).toInt()}% strength",
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) EditzColors.Purple.copy(alpha = 0.7f) else EditzColors.TextSecondary
            )
        }
    }
} 