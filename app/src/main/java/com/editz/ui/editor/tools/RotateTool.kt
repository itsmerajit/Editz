package com.editz.ui.editor.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Rotate90DegreesCcw
import androidx.compose.material.icons.filled.Rotate90DegreesCw
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

class RotateTool : VideoToolControls {
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
                text = "Rotate Video",
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rotate Left Button
                IconButton(
                    onClick = onValueChanged,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Rotate90DegreesCcw,
                        contentDescription = "Rotate Left",
                        tint = EditzColors.Purple,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Rotate Right Button
                IconButton(
                    onClick = onValueChanged,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Rotate90DegreesCw,
                        contentDescription = "Rotate Right",
                        tint = EditzColors.Purple,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Tap to rotate 90°",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary
            )
        }
    }
} 