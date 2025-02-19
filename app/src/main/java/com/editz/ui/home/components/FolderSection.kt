package com.editz.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

@Composable
fun FolderSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Folders",
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        LazyRow(
            modifier = Modifier.padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleFolders) { folder ->
                FolderItem(name = folder)
            }
        }
    }
}

@Composable
private fun FolderItem(
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.width(120.dp),
        shape = RoundedCornerShape(12.dp),
        color = EditzColors.Surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = EditzColors.Purple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextPrimary
            )
        }
    }
}

private val sampleFolders = listOf(
    "Recent",
    "Projects",
    "Exports",
    "Templates"
) 