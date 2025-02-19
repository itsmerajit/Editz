package com.editz.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import javax.inject.Inject

class VideoPickerHandler @Inject constructor() {
    
    fun getVideoDetails(context: Context, uri: Uri): VideoDetails? {
        try {
            // First try to get basic info
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)) ?: "Unknown"
                    
                    // Now try to get media details
                    val mediaMetadataRetriever = android.media.MediaMetadataRetriever().apply {
                        setDataSource(context, uri)
                    }
                    
                    val duration = try {
                        mediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                    
                    val width = try {
                        mediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) ?: "0"
                    } catch (e: Exception) {
                        "0"
                    }
                    
                    val height = try {
                        mediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT) ?: "0"
                    } catch (e: Exception) {
                        "0"
                    }
                    
                    val size = try {
                        context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                    
                    mediaMetadataRetriever.release()
                    
                    return VideoDetails(
                        uri = uri,
                        name = name,
                        duration = duration,
                        size = size,
                        resolution = "${width}x${height}"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}

data class VideoDetails(
    val uri: Uri,
    val name: String,
    val duration: Long,
    val size: Long,
    val resolution: String
) 