package com.editz.ui.preview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.editz.utils.VideoDetails
import javax.inject.Inject

@HiltViewModel
class VideoPreviewViewModel @Inject constructor() : ViewModel() {
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _videoDetails = MutableStateFlow<VideoDetails?>(null)
    val videoDetails: StateFlow<VideoDetails?> = _videoDetails.asStateFlow()

    fun initializeVideo(details: VideoDetails) {
        _videoDetails.value = details
        _currentPosition.value = 0L
        _isPlaying.value = false
    }

    fun updatePlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    fun updatePosition(position: Long) {
        _currentPosition.value = position
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up any resources
        _videoDetails.value = null
        _currentPosition.value = 0L
        _isPlaying.value = false
    }
} 