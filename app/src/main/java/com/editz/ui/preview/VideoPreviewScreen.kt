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
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import com.editz.theme.EditzColors
import com.editz.utils.VideoDetails
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VideoPreviewScreen(
    videoDetails: VideoDetails,
    volume: Float = 1f,
    speed: Float = 1f,
    startMs: Long = 0L,
    endMs: Long = 0L,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: VideoPreviewViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentPosition by remember { mutableStateOf(0L) }
    
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS / 2,  // Reduce minimum buffer
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS / 2,  // Reduce maximum buffer
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS / 2,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS / 2
                    )
                    .setPrioritizeTimeOverSizeThresholds(true)  // Prioritize time over size
                    .build()
            )
            .build().apply {
                // Video configuration
                videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                
                // Create media item with proper configuration
                val mediaItem = MediaItem.Builder()
                    .setUri(videoDetails.uri)
                    .setMimeType("video/*")
                    .build()
                
                // Clear any existing items
                clearMediaItems()
                setMediaItem(mediaItem)
                
                // Prepare but don't play automatically
                prepare()
                playWhenReady = false
                
                // Set audio attributes
                setAudioAttributes(
                    androidx.media3.common.AudioAttributes.Builder()
                        .setContentType(androidx.media3.common.C.CONTENT_TYPE_MOVIE)
                        .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                        .build(),
                    true
                )
                
                // Set repeat mode
                repeatMode = Player.REPEAT_MODE_OFF

                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                        isPlaying = isPlayingNow
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        if (endMs > 0 && newPosition.positionMs >= endMs) {
                            pause()
                            seekTo(startMs)
                        }
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                seekTo(if (startMs > 0) startMs else 0L)
                            }
                            Player.STATE_ENDED -> {
                                seekTo(startMs)
                                pause()
                            }
                            Player.STATE_BUFFERING -> {
                                // Reset position if needed
                                if (currentPosition >= endMs) {
                                    seekTo(startMs)
                                }
                            }
                            Player.STATE_IDLE -> {
                                // Try to recover
                                prepare()
                            }
                        }
                    }
                })
            }
    }

    // Update player parameters when they change
    LaunchedEffect(volume, speed, startMs, endMs) {
        exoPlayer.apply {
            this.volume = volume
            setPlaybackSpeed(speed)
            
            // Handle trim points
            if (startMs >= 0) {
                val targetPosition = if (currentPosition < startMs || currentPosition > endMs) {
                    startMs
                } else {
                    currentPosition
                }
                seekTo(targetPosition)
            }
        }
    }

    // Update current position periodically with error handling
    LaunchedEffect(Unit) {
        while (true) {
            try {
                delay(50) // Reduce update frequency further to 20fps
                if (isPlaying && !exoPlayer.isLoading) {
                    val position = exoPlayer.currentPosition
                    if (position >= 0) {
                        currentPosition = position
                        
                        // Check if we need to loop
                        if (endMs > 0 && position >= endMs) {
                            exoPlayer.seekTo(startMs)
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()
                exoPlayer.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditzColors.Background)
            .padding(16.dp)
    ) {
        // Video Preview with surface management
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
                        setShowBuffering(PlayerView.SHOW_BUFFERING_ALWAYS)
                        
                        // Surface configuration
                        setKeepContentOnPlayerReset(true)
                        useArtwork = false
                        
                        // Video surface properties
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.player = exoPlayer
                }
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
        
        // Basic Controls with error handling
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    try {
                        if (isPlaying) {
                            exoPlayer.pause()
                        } else {
                            if (currentPosition >= endMs) {
                                exoPlayer.seekTo(startMs)
                            }
                            exoPlayer.play()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
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
        
        // Seek Bar with error handling
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { position ->
                try {
                    val newPosition = position.toLong().coerceIn(
                        startMs,
                        if (endMs > 0) endMs else videoDetails.duration
                    )
                    currentPosition = newPosition
                    exoPlayer.seekTo(newPosition)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
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