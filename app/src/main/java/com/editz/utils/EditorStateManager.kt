package com.editz.utils

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.editz.ui.editor.model.VideoAdjustments
import com.editz.ui.editor.model.EffectState
import com.editz.ui.editor.model.VideoEffect
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditorStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun saveEditorState(
        videoUri: Uri,
        trimStartMs: Long,
        trimEndMs: Long,
        currentPosition: Long,
        volume: Float,
        speed: Float,
        rotation: Int,
        adjustments: VideoAdjustments,
        effectState: EffectState
    ) {
        prefs.edit().apply {
            putString(KEY_LAST_VIDEO_URI, videoUri.toString())
            putLong(KEY_TRIM_START, trimStartMs)
            putLong(KEY_TRIM_END, trimEndMs)
            putLong(KEY_POSITION, currentPosition)
            putFloat(KEY_VOLUME, volume)
            putFloat(KEY_SPEED, speed)
            putInt(KEY_ROTATION, rotation)
            putString(KEY_ADJUSTMENTS, gson.toJson(adjustments))
            putString(KEY_EFFECT_STATE, gson.toJson(effectState))
        }.apply()
    }

    fun getLastEditorState(): EditorState {
        return EditorState(
            videoUri = prefs.getString(KEY_LAST_VIDEO_URI, null)?.let { Uri.parse(it) },
            trimStartMs = prefs.getLong(KEY_TRIM_START, 0L),
            trimEndMs = prefs.getLong(KEY_TRIM_END, 0L),
            currentPosition = prefs.getLong(KEY_POSITION, 0L),
            volume = prefs.getFloat(KEY_VOLUME, 1f),
            speed = prefs.getFloat(KEY_SPEED, 1f),
            rotation = prefs.getInt(KEY_ROTATION, 0),
            adjustments = prefs.getString(KEY_ADJUSTMENTS, null)?.let {
                gson.fromJson(it, VideoAdjustments::class.java)
            } ?: VideoAdjustments(),
            effectState = prefs.getString(KEY_EFFECT_STATE, null)?.let {
                gson.fromJson(it, EffectState::class.java)
            } ?: EffectState()
        )
    }

    fun clearEditorState() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "editor_state"
        private const val KEY_LAST_VIDEO_URI = "last_video_uri"
        private const val KEY_TRIM_START = "trim_start"
        private const val KEY_TRIM_END = "trim_end"
        private const val KEY_POSITION = "position"
        private const val KEY_VOLUME = "volume"
        private const val KEY_SPEED = "speed"
        private const val KEY_ROTATION = "rotation"
        private const val KEY_ADJUSTMENTS = "adjustments"
        private const val KEY_EFFECT_STATE = "effect_state"
    }
}

data class EditorState(
    val videoUri: Uri? = null,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 0L,
    val currentPosition: Long = 0L,
    val volume: Float = 1f,
    val speed: Float = 1f,
    val rotation: Int = 0,
    val adjustments: VideoAdjustments = VideoAdjustments(),
    val effectState: EffectState = EffectState()
) 