package com.editz.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

@Composable
fun EditzTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = EditzColors.Purple,
            background = EditzColors.Background,
            surface = EditzColors.Surface,
            onBackground = EditzColors.TextPrimary,
            onSurface = EditzColors.TextPrimary
        ),
        content = content
    )
} 