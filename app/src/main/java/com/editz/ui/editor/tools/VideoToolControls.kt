package com.editz.ui.editor.tools

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface VideoToolControls {
    @Composable
    fun Content(
        modifier: Modifier,
        onValueChanged: () -> Unit
    )
}

// Base data class for tool states
data class ToolState(
    val isActive: Boolean = false,
    val intensity: Float = 1f
) 