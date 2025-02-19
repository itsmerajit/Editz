package com.editz.ui.preview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.editz.theme.EditzColors
import com.editz.utils.VideoDetails

@Composable
fun VideoPreviewScreen(
    videoDetails: VideoDetails,
    volume: Float = 1f,
    speed: Float = 1f,
    startMs: Long = 0L,
    endMs: Long = 0L,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoDetails.uri))
            prepare()
            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                    isPlaying = isPlayingNow
                }

                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    // Loop within trim points
                    if (endMs > 0 && newPosition.positionMs >= endMs) {
                        seekTo(startMs)
                    }
                }
            })
        }
    }

    // Update volume and speed when they change
    LaunchedEffect(volume) {
        exoPlayer.volume = volume
    }

    LaunchedEffect(speed) {
        exoPlayer.setPlaybackSpeed(speed)
    }

    // Update trim points
    LaunchedEffect(startMs, endMs) {
        if (startMs >= 0) {
            exoPlayer.seekTo(startMs)
        }
        if (endMs > 0) {
            // Set repeat mode to loop within trim points
            exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
            .padding(16.dp)
    ) {
        // Video Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f/9f)
        ) {
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        player = exoPlayer
                        useController = false
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Video Info
        Text(
            text = videoDetails.name,
            style = MaterialTheme.typography.titleMedium,
            color = EditzColors.TextPrimary
        )
        
        Text(
            text = "Duration: ${formatDuration(if (endMs > 0) endMs - startMs else videoDetails.duration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = EditzColors.TextSecondary
        )
        
        Text(
            text = "Resolution: ${videoDetails.resolution}",
            style = MaterialTheme.typography.bodyMedium,
            color = EditzColors.TextSecondary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Basic Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    if (isPlaying) {
                        exoPlayer.pause()
                    } else {
                        if (endMs > 0 && exoPlayer.currentPosition >= endMs) {
                            exoPlayer.seekTo(startMs)
                        }
                        exoPlayer.play()
                    }
                }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = EditzColors.Purple,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Seek Bar
        Slider(
            value = exoPlayer.currentPosition.toFloat(),
            onValueChange = { position ->
                exoPlayer.seekTo(position.toLong().coerceIn(startMs, if (endMs > 0) endMs else videoDetails.duration))
            },
            valueRange = startMs.toFloat()..if (endMs > 0) endMs.toFloat() else videoDetails.duration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = EditzColors.Purple,
                activeTrackColor = EditzColors.Purple,
                inactiveTrackColor = EditzColors.Surface
            )
        )
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