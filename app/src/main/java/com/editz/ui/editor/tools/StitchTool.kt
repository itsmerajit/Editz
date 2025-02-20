package com.editz.ui.editor.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

class StitchTool : VideoToolControls {
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        Column(
            modifier = modifier.padding(16.dp)
        ) {
            Text(
                text = "Stitch Videos",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(3) { index ->
                    VideoThumbnail(
                        index = index + 1,
                        onValueChanged = onValueChanged
                    )
                }
                
                item {
                    AddVideoButton(onValueChanged = onValueChanged)
                }
            }
        }
    }
}

@Composable
private fun VideoThumbnail(
    index: Int,
    onValueChanged: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .padding(4.dp)
    ) {
        Surface(
            color = EditzColors.Surface,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Video $index",
                    color = EditzColors.TextPrimary
                )
            }
        }
    }
}

@Composable
private fun AddVideoButton(
    onValueChanged: () -> Unit
) {
    IconButton(
        onClick = onValueChanged,
        modifier = Modifier.size(120.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Video",
            tint = EditzColors.Purple
        )
    }
} 