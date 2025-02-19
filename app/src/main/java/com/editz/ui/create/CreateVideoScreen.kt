package com.editz.ui.create

import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.editz.theme.EditzColors
import com.editz.ui.preview.VideoPreviewScreen
import com.editz.data.VideoDetails

@Composable
fun CreateVideoScreen(
    onPickVideo: () -> Unit = {},
    onNavigateToEditor: (VideoDetails) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: CreateVideoViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        viewModel.onPermissionsResult(allGranted)
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            viewModel.onVideoSelected(context, it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.checkPermissions(context)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
    ) {
        when (uiState) {
            is CreateVideoUiState.Initial -> {
                // Loading state if needed
            }
            is CreateVideoUiState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = EditzColors.Purple
                )
            }
            is CreateVideoUiState.PermissionsGranted -> {
                PickVideoCard(
                    modifier = Modifier.padding(16.dp)
                ) {
                    videoPickerLauncher.launch("video/*")
                }
            }
            is CreateVideoUiState.NeedsPermissions -> {
                PermissionCard(
                    modifier = Modifier.padding(16.dp)
                ) {
                    permissionLauncher.launch(
                        (uiState as CreateVideoUiState.NeedsPermissions)
                            .permissions.toTypedArray()
                    )
                }
            }
            is CreateVideoUiState.PermissionsDenied -> {
                PermissionDeniedCard(
                    modifier = Modifier.padding(16.dp)
                )
            }
            is CreateVideoUiState.VideoSelected -> {
                LaunchedEffect(Unit) {
                    onNavigateToEditor((uiState as CreateVideoUiState.VideoSelected).videoDetails)
                }
            }
            is CreateVideoUiState.Error -> {
                ErrorCard(
                    message = (uiState as CreateVideoUiState.Error).message,
                    onRetry = {
                        videoPickerLauncher.launch("video/*")
                    },
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun PickVideoCard(
    modifier: Modifier = Modifier,
    onPickVideo: () -> Unit
) {
    Card(
        modifier = modifier
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

@Composable
private fun PermissionCard(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Card(
        modifier = modifier
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
                imageVector = Icons.Default.Lock,
                contentDescription = "Permissions needed",
                modifier = Modifier.size(48.dp),
                tint = EditzColors.Purple
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Storage Permission Required",
                style = MaterialTheme.typography.titleLarge,
                color = EditzColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "We need permission to access your videos",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EditzColors.Purple
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Grant Permission",
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun PermissionDeniedCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                imageVector = Icons.Default.Lock,
                contentDescription = "Permissions denied",
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Permission Denied",
                style = MaterialTheme.typography.titleLarge,
                color = EditzColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Please enable storage permission in settings to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = EditzColors.TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = Color.Red
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = EditzColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EditzColors.Purple
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Try Again",
                    color = Color.White
                )
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = durationMs / (1000 * 60 * 60)
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
} 