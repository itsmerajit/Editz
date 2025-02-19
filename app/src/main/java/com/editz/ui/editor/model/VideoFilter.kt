package com.editz.ui.editor.model

enum class VideoFilter(val displayName: String, val intensity: Float = 1f) {
    ORIGINAL("Original"),
    VINTAGE("Vintage", 0.7f),
    DRAMATIC("Dramatic", 1.2f),
    COOL("Cool", 0.8f),
    WARM("Warm", 0.8f),
    VIBRANT("Vibrant", 1.3f),
    MUTED("Muted", 0.6f)
} 