package com.editz.ui.editor

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editz.data.VideoDetails
import com.editz.ui.editor.model.VideoFilter
import com.editz.ui.editor.model.VideoAdjustments
import com.editz.ui.editor.model.EffectState
import com.editz.ui.editor.model.VideoEffect
import com.editz.utils.VideoProcessor
import com.editz.utils.EditorStateManager
import com.editz.data.manager.VideoSaveManager
import com.editz.data.manager.VideoSaveManager.SaveResult
import com.editz.util.PermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class VideoEditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val videoProcessor: VideoProcessor,
    private val editorStateManager: EditorStateManager,
    private val videoSaveManager: VideoSaveManager,
    private val permissionChecker: PermissionChecker
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

    private val _uiState = MutableStateFlow<VideoEditorUiState>(VideoEditorUiState.Initial)
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

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
                if (!checkPermissions()) {
                    _uiState.value = VideoEditorUiState.RequiresPermission
                    _processingError.value = "Storage permission required to save video"
                    return@launch
                }

                _isProcessing.value = true
                _processingError.value = null
                _progress.value = 0f
                _uiState.value = VideoEditorUiState.Processing(0f)
                
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(Date())
                val outputFile = File(
                    context.getExternalFilesDir(null),
                    "editz_${timestamp}.mp4"
                )
                
                videoProcessor.processVideo(
                    inputUri = videoDetails.value?.uri ?: run {
                        _processingError.value = "No video selected"
                        return@launch
                    },
                    outputFile = outputFile,
                    trimStartMs = _trimStartMs.value,
                    trimEndMs = _trimEndMs.value,
                    filter = _currentFilter.value,
                    speed = _speed.value,
                    volume = _volume.value,
                    rotation = _rotation.value,
                    progressCallback = { progress ->
                        _progress.value = progress * 0.8f // Processing is 80% of total progress
                        _uiState.value = VideoEditorUiState.Processing(progress * 0.8f)
                    }
                ).onSuccess { file ->
                    handleVideoSaving(file.absolutePath)
                }.onFailure { error ->
                    _processingError.value = "Failed to process video: ${error.message}"
                    _uiState.value = VideoEditorUiState.SaveError("Failed to process video: ${error.message}")
                }
            } catch (e: Exception) {
                _processingError.value = "An unexpected error occurred: ${e.message}"
                _uiState.value = VideoEditorUiState.SaveError("An unexpected error occurred: ${e.message}")
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    private fun handleVideoSaving(sourceFilePath: String) {
        viewModelScope.launch {
            val fileName = generateFileName()
            videoSaveManager.saveVideo(sourceFilePath, fileName)
                .collect { result ->
                    _uiState.value = when (result) {
                        is SaveResult.Success -> {
                            _progress.value = 1f
                            try {
                                File(sourceFilePath).delete()
                            } catch (e: Exception) {
                                // Ignore cleanup errors
                            }
                            VideoEditorUiState.SaveSuccess
                        }
                        is SaveResult.Error -> VideoEditorUiState.SaveError(result.message)
                        SaveResult.InProgress -> {
                            _progress.value = 0.8f
                            VideoEditorUiState.Saving(0.8f)
                        }
                        is SaveResult.Progress -> {
                            val totalProgress = 0.8f + (result.progress * 0.2f)
                            _progress.value = totalProgress
                            VideoEditorUiState.Saving(totalProgress)
                        }
                    }
                }
        }
    }
    
    fun clearError() {
        _processingError.value = null
    }

    private fun checkPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionChecker.hasPermission(android.Manifest.permission.READ_MEDIA_VIDEO) &&
            permissionChecker.hasPermission(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            permissionChecker.hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) &&
            permissionChecker.hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun generateFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        return "EDITZ_$timestamp.mp4"
    }
    
    companion object {
        const val MIN_TRIM_DURATION = 1000L // 1 second
    }
}

sealed class VideoEditorUiState {
    object Initial : VideoEditorUiState()
    data class Processing(val progress: Float) : VideoEditorUiState()
    object RequiresPermission : VideoEditorUiState()
    data class Saving(val progress: Float) : VideoEditorUiState()
    object SaveSuccess : VideoEditorUiState()
    data class SaveError(val message: String) : VideoEditorUiState()
    
    data class Editing(
        val videoDetails: VideoDetails,
        val volume: Float = 1f,
        val speed: Float = 1f,
        val filter: String? = null,
        val trimStartMs: Long = 0L,
        val trimEndMs: Long = 0L
    ) : VideoEditorUiState()
} 