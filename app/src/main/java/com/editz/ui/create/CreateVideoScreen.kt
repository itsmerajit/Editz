package com.editz.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.editz.theme.EditzColors

@Composable
fun CreateVideoScreen(
    onPickVideo: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Video Pick Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = EditzColors.Surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = "Pick Video",
                    modifier = Modifier.size(48.dp),
                    tint = EditzColors.Purple
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Pick a Video to Start",
                    style = MaterialTheme.typography.titleLarge,
                    color = EditzColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Select a video from your device to begin editing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EditzColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onPickVideo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EditzColors.Purple
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Choose Video",
                        color = Color.White
                    )
                }
            }
        }
    }
} 