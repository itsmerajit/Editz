package com.editz.ui.editor.tools.stitch

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.tools.VideoToolControls

class StitchTool : VideoToolControls {
    @Composable
    override fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    ) {
        Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Stitch Videos",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // TODO: Implement video stitching UI
            Text(
                text = "Coming soon...",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
        }
    }
} 