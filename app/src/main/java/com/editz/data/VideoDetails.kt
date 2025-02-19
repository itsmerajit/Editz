package com.editz.data

import android.net.Uri

data class VideoDetails(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val width: Int,
    val height: Int,
    val size: Long,
    val mimeType: String,
    val thumbnailPath: String? = null
) 