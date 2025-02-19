package com.editz.ui.editor

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.editz.utils.VideoDetails
import com.editz.ui.editor.components.VideoFilter
import com.editz.ui.editor.components.VideoAdjustments
import com.editz.ui.editor.components.VideoEffect
import javax.inject.Inject

data class EffectState(
    val effect: VideoEffect = VideoEffect.NONE,
    val intensity: Float = 1f
)

@HiltViewModel
class VideoEditorViewModel @Inject constructor() : ViewModel() {
    private val _videoDetails = MutableStateFlow<VideoDetails?>(null)
    val videoDetails: StateFlow<VideoDetails?> = _videoDetails.asStateFlow()

    private val _currentFilter = MutableStateFlow(VideoFilter.NONE)
    val currentFilter: StateFlow<VideoFilter> = _currentFilter.asStateFlow()

    private val _adjustments = MutableStateFlow(VideoAdjustments())
    val adjustments: StateFlow<VideoAdjustments> = _adjustments.asStateFlow()

    private val _effectState = MutableStateFlow(EffectState())
    val effectState: StateFlow<EffectState> = _effectState.asStateFlow()

    private var currentVolume: Float = 1f
    private var currentSpeed: Float = 1f
    private var trimStartMs: Long = 0L
    private var trimEndMs: Long = 0L

    fun initializeEditor(details: VideoDetails) {
        _videoDetails.value = details
        _currentFilter.value = VideoFilter.NONE
        _adjustments.value = VideoAdjustments()
        _effectState.value = EffectState()
        currentVolume = 1f
        currentSpeed = 1f
        trimStartMs = 0L
        trimEndMs = details.duration
    }

    fun updateVolume(volume: Float) {
        currentVolume = volume
    }

    fun updateSpeed(speed: Float) {
        currentSpeed = speed
    }

    fun updateFilter(filter: VideoFilter) {
        _currentFilter.value = filter
    }

    fun updateTrimPoints(startMs: Long, endMs: Long) {
        trimStartMs = startMs
        trimEndMs = endMs
    }

    fun updateEffect(effect: VideoEffect) {
        _effectState.value = _effectState.value.copy(effect = effect)
    }

    fun updateEffectIntensity(intensity: Float) {
        _effectState.value = _effectState.value.copy(intensity = intensity)
    }

    fun updateAdjustments(adjustments: VideoAdjustments) {
        _adjustments.value = adjustments
    }

    fun resetEdits() {
        _currentFilter.value = VideoFilter.NONE
        _adjustments.value = VideoAdjustments()
        _effectState.value = EffectState()
    }

    override fun onCleared() {
        super.onCleared()
        _videoDetails.value = null
        resetEdits()
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