package com.editz.ui.create

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.editz.utils.PermissionHandler
import com.editz.utils.VideoDetails
import com.editz.utils.VideoPickerHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateVideoViewModel @Inject constructor(
    private val permissionHandler: PermissionHandler,
    private val videoPickerHandler: VideoPickerHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateVideoUiState>(CreateVideoUiState.Initial)
    val uiState: StateFlow<CreateVideoUiState> = _uiState.asStateFlow()

    fun checkPermissions(context: Context) {
        if (permissionHandler.hasPermissions(context)) {
            _uiState.value = CreateVideoUiState.PermissionsGranted
        } else {
            val permissions = permissionHandler.getMissingPermissions(context)
            _uiState.value = CreateVideoUiState.NeedsPermissions(permissions)
        }
    }

    fun onPermissionsResult(granted: Boolean) {
        _uiState.value = if (granted) {
            CreateVideoUiState.PermissionsGranted
        } else {
            CreateVideoUiState.PermissionsDenied
        }
    }

    fun onVideoSelected(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = CreateVideoUiState.Loading
            
            videoPickerHandler.getVideoDetails(context, uri)?.let { videoDetails ->
                _uiState.value = CreateVideoUiState.VideoSelected(videoDetails)
            } ?: run {
                _uiState.value = CreateVideoUiState.Error("Failed to load video details")
            }
        }
    }
}

sealed class CreateVideoUiState {
    object Initial : CreateVideoUiState()
    object Loading : CreateVideoUiState()
    object PermissionsGranted : CreateVideoUiState()
    data class NeedsPermissions(val permissions: List<String>) : CreateVideoUiState()
    object PermissionsDenied : CreateVideoUiState()
    data class VideoSelected(val videoDetails: VideoDetails) : CreateVideoUiState()
    data class Error(val message: String) : CreateVideoUiState()
} 