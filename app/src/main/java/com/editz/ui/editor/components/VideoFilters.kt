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
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Filters",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) EditzColors.Purple.copy(alpha = 0.1f)
                else EditzColors.Surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
            .width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(EditzColors.Surface)
        ) {
            // Placeholder for filter preview
            // TODO: Add actual filter preview
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = filter.displayName,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) EditzColors.Purple else EditzColors.TextSecondary
        )
    }
} 