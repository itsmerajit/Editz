package com.editz.ui.editor.tools.rotate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors
import com.editz.ui.editor.tools.VideoToolControls

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
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Rotate Left Button
                Button(
                    onClick = {
                        // Handle rotate left
                        onValueChanged()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditzColors.Surface
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Text(
                        text = "⟲",
                        color = EditzColors.TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                
                // Rotate Right Button
                Button(
                    onClick = {
                        // Handle rotate right
                        onValueChanged()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditzColors.Surface
                    ),
                    modifier = Modifier.size(64.dp)
                ) {
                    Text(
                        text = "⟳",
                        color = EditzColors.TextPrimary,
                        style = MaterialTheme.typography.titleLarge
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