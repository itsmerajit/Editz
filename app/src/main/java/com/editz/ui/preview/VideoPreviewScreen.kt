package com.editz.ui.preview

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import android.view.View

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
    
    // Use viewModel to handle video state
    LaunchedEffect(Unit) {
        viewModel.initializeVideo(videoDetails)
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        500,  // Minimum buffer
                        1500, // Maximum buffer
                        500,  // Buffer for playback
                        500   // Buffer for playback after rebuffer
                    )
                    .setBackBuffer(500, true)
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            )
            .setVideoScalingMode(androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT)
            .setReleaseTimeoutMs(2000)
            .build().apply {
                // Clear any existing items and memory
                stop()
                clearMediaItems()
                clearVideoSurface()
                
                // Set video scaling mode for better performance
                videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                
                // Create media item with proper configuration
                val mediaItem = MediaItem.Builder()
                    .setUri(videoDetails.uri)
                    .setMimeType("video/*")
                    .build()
                
                setMediaItem(mediaItem)
                prepare()
                
                // Optimize playback settings
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_OFF
                
                // Add optimized listener
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                        isPlaying = isPlayingNow
                    }

                    override fun onPlaybackStateChanged(state: Int) {
                        when (state) {
                            Player.STATE_READY -> {
                                if (startMs > 0) {
                                    seekTo(startMs)
                                }
                            }
                            Player.STATE_ENDED -> {
                                seekTo(startMs)
                                pause()
                            }
                            Player.STATE_BUFFERING -> {
                                // Only seek if really needed
                                if (currentPosition >= endMs) {
                                    seekTo(startMs)
                                }
                            }
                            Player.STATE_IDLE -> {
                                // Minimal recovery
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

    // Optimize position updates
    LaunchedEffect(Unit) {
        while (true) {
            try {
                delay(50) // Update at 20fps for smoother UI
                if (isPlaying && !exoPlayer.isLoading) {
                    val position = exoPlayer.currentPosition
                    if (position >= 0) {
                        currentPosition = position
                        
                        // Optimize trim point handling
                        if (endMs > 0 && position >= endMs - 50) { // Add small buffer
                            exoPlayer.pause()
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
                exoPlayer.clearVideoSurface()
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
        // Add back button
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = EditzColors.TextPrimary
            )
        }

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
                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                        
                        // Remove deprecated useArtwork
                        setShowPreviousButton(false)
                        setShowNextButton(false)
                        
                        // Set optimal surface properties
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        
                        // Set player
                        this.player = exoPlayer
                        
                        // Initial surface setup
                        videoSurfaceView?.let { surfaceView ->
                            if (surfaceView is android.view.SurfaceView) {
                                exoPlayer.setVideoSurfaceView(surfaceView)
                            }
                        }
                        
                        // Add cleanup listener
                        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                            override fun onViewAttachedToWindow(v: View) {
                                if (v is PlayerView) {
                                    v.videoSurfaceView?.let { surfaceView ->
                                        if (surfaceView is android.view.SurfaceView) {
                                            exoPlayer.setVideoSurfaceView(surfaceView)
                                        }
                                    }
                                }
                            }
                            
                            override fun onViewDetachedFromWindow(v: View) {
                                exoPlayer.clearVideoSurface()
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.player = exoPlayer
                    view.keepScreenOn = isPlaying
                    
                    // Ensure surface is set after updates
                    view.videoSurfaceView?.let { surfaceView ->
                        if (surfaceView is android.view.SurfaceView) {
                            exoPlayer.setVideoSurfaceView(surfaceView)
                        }
                    }
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