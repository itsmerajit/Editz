package com.editz.ui.editor.model

data class VideoAdjustments(
    val brightness: Float = 0f,
    val contrast: Float = 0f,
    val saturation: Float = 0f,
    val warmth: Float = 0f
)

data class EffectState(
    val effect: VideoEffect = VideoEffect.NONE,
    val intensity: Float = 0f
)

enum class VideoEffect {
    NONE,
    BLUR,
    VIGNETTE,
    GRAIN,
    GLITCH,
    PIXELATE
}

enum class VideoTool {
    TRIM,
    FILTER,
    EFFECT,
    ADJUST,
    EXPORT
} 