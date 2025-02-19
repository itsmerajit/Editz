package com.editz.ui.editor

import androidx.lifecycle.ViewModel
import com.editz.utils.VideoDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class VideoEditorViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow<VideoEditorUiState>(VideoEditorUiState.Initial)
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private var currentVolume: Float = 1f
    private var currentSpeed: Float = 1f
    private var currentFilter: String? = null
    private var trimStartMs: Long = 0L
    private var trimEndMs: Long = 0L

    fun initializeEditor(videoDetails: VideoDetails) {
        _uiState.value = VideoEditorUiState.Editing(
            videoDetails = videoDetails,
            volume = currentVolume,
            speed = currentSpeed,
            filter = currentFilter,
            trimStartMs = trimStartMs,
            trimEndMs = trimEndMs
        )
    }

    fun updateVolume(volume: Float) {
        currentVolume = volume
        updateState()
    }

    fun updateSpeed(speed: Float) {
        currentSpeed = speed
        updateState()
    }

    fun updateFilter(filter: String?) {
        currentFilter = filter
        updateState()
    }

    fun updateTrimPoints(startMs: Long, endMs: Long) {
        trimStartMs = startMs
        trimEndMs = endMs
        updateState()
    }

    private fun updateState() {
        val currentState = uiState.value
        if (currentState is VideoEditorUiState.Editing) {
            _uiState.value = currentState.copy(
                volume = currentVolume,
                speed = currentSpeed,
                filter = currentFilter,
                trimStartMs = trimStartMs,
                trimEndMs = trimEndMs
            )
        }
    }
}

sealed class VideoEditorUiState {
    object Initial : VideoEditorUiState()
    
    data class Editing(
        val videoDetails: VideoDetails,
        val volume: Float = 1f,
        val speed: Float = 1f,
        val filter: String? = null,
        val trimStartMs: Long = 0L,
        val trimEndMs: Long = 0L
    ) : VideoEditorUiState()
} 