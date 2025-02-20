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
import com.editz.ui.editor.tools.trim.VideoTrimmer

@Composable
fun VideoPreviewScreen(
    videoUri: Uri,
    volume: Float = 1f,
    speed: Float = 1f,
    isPlaying: Boolean = false,
    onPlayPause: (Boolean) -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isPlayerReady by remember { mutableStateOf(false) }
    
    // Effect to handle play/pause state changes
    LaunchedEffect(isPlaying, isPlayerReady) {
        if (isPlayerReady) {
            player?.playWhenReady = isPlaying
            android.util.Log.d("VideoPreview", "Setting playWhenReady: $isPlaying")
        }
    }
    
    // Effect to handle volume changes
    LaunchedEffect(volume) {
        player?.volume = volume
        android.util.Log.d("VideoPreview", "Setting volume: $volume")
    }
    
    // Effect to handle speed changes
    LaunchedEffect(speed) {
        player?.setPlaybackSpeed(speed)
        android.util.Log.d("VideoPreview", "Setting speed: $speed")
    }
    
    DisposableEffect(context) {
        android.util.Log.d("VideoPreview", "Creating new player")
        val newPlayer = ExoPlayer.Builder(context)
            .setLoadControl(DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    32 * 1024, // Min buffer
                    64 * 1024, // Max buffer
                    1024, // Buffer for playback
                    1024 // Buffer for rebuffer
                )
                .build()
            )
            .build()
            
        player = newPlayer
        
        // Add player listener
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> {
                        android.util.Log.d("VideoPreview", "Player STATE_READY")
                        isPlayerReady = true
                        onSeek(newPlayer.currentPosition)
                    }
                    Player.STATE_BUFFERING -> {
                        android.util.Log.d("VideoPreview", "Player STATE_BUFFERING")
                    }
                    Player.STATE_ENDED -> {
                        android.util.Log.d("VideoPreview", "Player STATE_ENDED")
                        onPlayPause(false)
                    }
                    Player.STATE_IDLE -> {
                        android.util.Log.d("VideoPreview", "Player STATE_IDLE")
                    }
                }
            }
            
            override fun onIsPlayingChanged(isActuallyPlaying: Boolean) {
                android.util.Log.d("VideoPreview", "onIsPlayingChanged: $isActuallyPlaying")
                onPlayPause(isActuallyPlaying)
            }
            
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("VideoPreview", "Player error: ${error.message}")
                error.printStackTrace()
            }
        }
        
        newPlayer.apply {
            addListener(listener)
            setMediaItem(MediaItem.fromUri(videoUri))
            android.util.Log.d("VideoPreview", "Setting media URI: $videoUri")
            prepare()
            this.volume = volume
            setPlaybackSpeed(speed)
            playWhenReady = isPlaying
            repeatMode = Player.REPEAT_MODE_ALL
        }
        
        onDispose {
            android.util.Log.d("VideoPreview", "Disposing player")
            newPlayer.removeListener(listener)
            newPlayer.release()
            player = null
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
                    this.player = player
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                    layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    setBackgroundColor(android.graphics.Color.BLACK)
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                view.player = player
            }
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