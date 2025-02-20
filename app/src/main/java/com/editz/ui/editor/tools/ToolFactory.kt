package com.editz.ui.editor.tools

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.editz.ui.editor.model.VideoTool
import com.editz.ui.editor.tools.speed.SpeedTool
import com.editz.ui.editor.tools.rotate.RotateTool
import com.editz.ui.editor.tools.stitch.StitchTool
import com.editz.ui.editor.tools.mask.MaskTool
import com.editz.ui.editor.tools.opacity.OpacityTool
import com.editz.ui.editor.tools.voice.VoiceEffectTool
import com.editz.ui.editor.tools.trim.TrimTool

object ToolFactory {
    fun createTool(tool: VideoTool): VideoToolControls {
        return when (tool) {
            VideoTool.TRIM -> TrimTool()
            VideoTool.ROTATE -> RotateTool()
            VideoTool.SPEED -> SpeedTool()
            VideoTool.VOICE_EFFECT -> VoiceEffectTool()
            VideoTool.OPACITY -> OpacityTool()
            VideoTool.MASK -> MaskTool()
            VideoTool.STITCH -> StitchTool()
            VideoTool.REPLACE -> ReplaceTool()
            VideoTool.DUPLICATE -> DuplicateTool()
            VideoTool.VOICE -> VoiceEffectTool()
        }
    }
}

// Placeholder tools that need to be implemented
class TrimTool : VideoToolControls {
    @Composable
    override fun Content(modifier: Modifier, onValueChanged: () -> Unit) {
        // TODO: Implement Trim tool UI
    }
}

class ReplaceTool : VideoToolControls {
    @Composable
    override fun Content(modifier: Modifier, onValueChanged: () -> Unit) {
        // TODO: Implement Replace tool UI
    }
}

class DuplicateTool : VideoToolControls {
    @Composable
    override fun Content(modifier: Modifier, onValueChanged: () -> Unit) {
        // TODO: Implement Duplicate tool UI
    }
} 