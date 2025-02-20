package com.editz.ui.editor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editz.data.VideoDetails
import com.editz.ui.editor.model.VideoFilter
import com.editz.ui.editor.model.VideoAdjustments
import com.editz.ui.editor.model.EffectState
import com.editz.ui.editor.model.VideoEffect
import com.editz.utils.VideoProcessor
import com.editz.utils.EditorStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoProcessor: VideoProcessor,
    private val editorStateManager: EditorStateManager
) : ViewModel() {
    private val _videoDetails = MutableStateFlow<VideoDetails?>(null)
    val videoDetails: StateFlow<VideoDetails?> = _videoDetails.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()
    
    private val _trimStartMs = MutableStateFlow(0L)
    val trimStartMs: StateFlow<Long> = _trimStartMs.asStateFlow()
    
    private val _trimEndMs = MutableStateFlow(0L)
    val trimEndMs: StateFlow<Long> = _trimEndMs.asStateFlow()
    
    private val _currentFilter = MutableStateFlow(VideoFilter.ORIGINAL)
    val currentFilter: StateFlow<VideoFilter> = _currentFilter.asStateFlow()
    
    private val _adjustments = MutableStateFlow(VideoAdjustments())
    val adjustments: StateFlow<VideoAdjustments> = _adjustments.asStateFlow()
    
    private val _effectState = MutableStateFlow(EffectState())
    val effectState: StateFlow<EffectState> = _effectState.asStateFlow()
    
    private val _speed = MutableStateFlow(1f)
    val speed: StateFlow<Float> = _speed.asStateFlow()
    
    private val _volume = MutableStateFlow(1f)
    val volume: StateFlow<Float> = _volume.asStateFlow()
    
    private val _rotation = MutableStateFlow(0)
    val rotation: StateFlow<Int> = _rotation.asStateFlow()
    
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()
    
    private val _processingError = MutableStateFlow<String?>(null)
    val processingError: StateFlow<String?> = _processingError.asStateFlow()
    
    fun initializeEditor(videoDetails: VideoDetails) {
        _videoDetails.value = videoDetails
        
        // Load last state if it's the same video
        val lastState = editorStateManager.getLastEditorState()
        if (lastState.videoUri == videoDetails.uri) {
            _trimStartMs.value = lastState.trimStartMs
            _trimEndMs.value = lastState.trimEndMs
            _currentPosition.value = lastState.currentPosition
            _volume.value = lastState.volume
            _speed.value = lastState.speed
            _rotation.value = lastState.rotation
            _adjustments.value = lastState.adjustments
            _effectState.value = lastState.effectState
        } else {
            // Reset to defaults for new video
            _trimStartMs.value = 0L
            _trimEndMs.value = videoDetails.duration
            resetEdits()
        }
    }
    
    fun updateTrimStart(startMs: Long) {
        _trimStartMs.value = startMs.coerceIn(0L, _trimEndMs.value - MIN_TRIM_DURATION)
        saveCurrentState()
    }
    
    fun updateTrimEnd(endMs: Long) {
        _trimEndMs.value = endMs.coerceIn(_trimStartMs.value + MIN_TRIM_DURATION, videoDetails.value?.duration ?: 0L)
        saveCurrentState()
    }
    
    fun updatePosition(position: Long) {
        _currentPosition.value = position
        saveCurrentState()
    }
    
    fun updateFilter(filter: VideoFilter) {
        _currentFilter.value = filter
        saveCurrentState()
    }
    
    fun updateEffect(effect: VideoEffect) {
        _effectState.value = _effectState.value.copy(effect = effect)
        saveCurrentState()
    }
    
    fun updateAdjustments(adjustments: VideoAdjustments) {
        _adjustments.value = adjustments
        saveCurrentState()
    }
    
    fun updateSpeed(speed: Float) {
        _speed.value = speed.coerceIn(0.25f, 2f)
        saveCurrentState()
    }
    
    fun updateVolume(volume: Float) {
        _volume.value = volume.coerceIn(0f, 1f)
        saveCurrentState()
    }
    
    fun updateRotation(degrees: Int) {
        _rotation.value = ((_rotation.value + degrees) % 360 + 360) % 360
        saveCurrentState()
    }
    
    private fun saveCurrentState() {
        videoDetails.value?.let { details ->
            editorStateManager.saveEditorState(
                videoUri = details.uri,
                trimStartMs = _trimStartMs.value,
                trimEndMs = _trimEndMs.value,
                currentPosition = _currentPosition.value,
                volume = _volume.value,
                speed = _speed.value,
                rotation = _rotation.value,
                adjustments = _adjustments.value,
                effectState = _effectState.value
            )
        }
    }
    
    fun resetEdits() {
        _currentFilter.value = VideoFilter.ORIGINAL
        _adjustments.value = VideoAdjustments()
        _effectState.value = EffectState()
        _trimStartMs.value = 0L
        _trimEndMs.value = videoDetails.value?.duration ?: 0L
        _speed.value = 1f
        _volume.value = 1f
        _rotation.value = 0
        saveCurrentState()
    }
    
    fun saveChanges() {
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _processingError.value = null
                
                val outputFile = File(
                    context.getExternalFilesDir(null),
                    "edited_video_${System.currentTimeMillis()}.mp4"
                )
                
                videoProcessor.processVideo(
                    inputUri = videoDetails.value?.uri ?: return@launch,
                    outputFile = outputFile,
                    trimStartMs = _trimStartMs.value,
                    trimEndMs = _trimEndMs.value,
                    filter = _currentFilter.value,
                    speed = _speed.value,
                    volume = _volume.value,
                    rotation = _rotation.value
                ).onSuccess { file ->
                    // TODO: Save file to media store
                }.onFailure { error ->
                    _processingError.value = error.message ?: "Failed to process video"
                }
            } catch (e: Exception) {
                _processingError.value = e.message ?: "An unexpected error occurred"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    fun clearError() {
        _processingError.value = null
    }
    
    companion object {
        const val MIN_TRIM_DURATION = 1000L // 1 second
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