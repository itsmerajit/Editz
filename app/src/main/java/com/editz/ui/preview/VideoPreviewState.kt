package com.editz.ui.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class VideoPreviewState {
    var isPlaying by mutableStateOf(false)
    var currentPosition by mutableStateOf(0L)
} 