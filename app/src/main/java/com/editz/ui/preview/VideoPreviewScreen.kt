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
import com.editz.data.VideoDetails
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.compose.ui.graphics.Color

@Composable
fun VideoPreviewScreen(
    videoDetails: VideoDetails,
    volume: Float = 1f,
    speed: Float = 1f,
    startMs: Long = 0L,
    endMs: Long = 0L,
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentPosition by remember { mutableStateOf(0L) }
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    val playerReadyState = remember { mutableStateOf(false) }
    var isPlayerReady by playerReadyState

    // Create ExoPlayer instance
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS,
                        DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                    )
                    .build()
            )
            .build()
    }

    // Initialize player with media
    LaunchedEffect(videoDetails) {
        with(exoPlayer) {
            setMediaItem(MediaItem.fromUri(videoDetails.uri))
            videoScalingMode = androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            repeatMode = Player.REPEAT_MODE_OFF
            playWhenReady = false
            prepare()
        }
    }

    // Set up player listener
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_READY -> {
                        playerReadyState.value = true
                        if (startMs > 0) {
                            exoPlayer.seekTo(startMs)
                        }
                    }
                    Player.STATE_ENDED -> {
                        exoPlayer.seekTo(startMs)
                        exoPlayer.pause()
                        onPlayPause()
                    }
                    Player.STATE_BUFFERING -> {
                        // Keep the ready state true during buffering to prevent interruption
                        if (isPlayerReady) {
                            playerReadyState.value = true
                        }
                    }
                    Player.STATE_IDLE -> {
                        playerReadyState.value = false
                    }
                }
            }
        })
    }

    // Handle play state changes
    LaunchedEffect(isPlaying) {
        if (isPlayerReady) {
            if (isPlaying) {
                exoPlayer.play()
            } else {
                exoPlayer.pause()
            }
        }
    }

    // Handle position updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(32) // ~30fps
            if (isPlaying && isPlayerReady) {
                val position = exoPlayer.currentPosition
                if (position >= 0) {
                    currentPosition = position
                    onSeek(position)
                    
                    // Check if we've reached the end position
                    if (endMs > 0 && position >= endMs) {
                        exoPlayer.pause()
                        exoPlayer.seekTo(startMs)
                        onPlayPause()
                    }
                }
            }
        }
    }

    // Handle volume and speed changes
    LaunchedEffect(volume, speed) {
        exoPlayer.setVolume(volume)
        exoPlayer.setPlaybackSpeed(speed)
    }

    // Handle start and end position changes
    LaunchedEffect(startMs, endMs) {
        if (isPlayerReady) {
            if (currentPosition < startMs || currentPosition > endMs) {
                exoPlayer.seekTo(startMs)
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.stop()
            playerView?.player = null
            exoPlayer.release()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    
                    setShutterBackgroundColor(android.graphics.Color.TRANSPARENT)
                    setKeepContentOnPlayerReset(true)
                    
                    playerView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.player = exoPlayer
                view.keepScreenOn = isPlaying
            }
        )
        
        if (!isPlayerReady) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
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