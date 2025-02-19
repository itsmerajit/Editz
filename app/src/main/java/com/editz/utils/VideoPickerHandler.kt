package com.editz.utils

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.editz.data.VideoDetails
import javax.inject.Inject

class VideoPickerHandler @Inject constructor() {
    
    fun getVideoDetails(context: Context, uri: Uri): VideoDetails? {
        try {
            println("DEBUG: Starting to get video details for uri: $uri")
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
                    println("DEBUG: Got video name: $name")
                    
                    // Now try to get media details
                    val mediaMetadataRetriever = android.media.MediaMetadataRetriever().apply {
                        setDataSource(context, uri)
                    }
                    
                    val duration = try {
                        mediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                    } catch (e: Exception) {
                        println("DEBUG: Error getting duration: ${e.message}")
                        0L
                    }
                    
                    println("DEBUG: Extracted metadata - Duration: $duration")
                    
                    val width = try {
                        mediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    
                    val height = try {
                        mediaMetadataRetriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt() ?: 0
                    } catch (e: Exception) {
                        0
                    }
                    
                    val size = try {
                        context.contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0L
                    } catch (e: Exception) {
                        0L
                    }
                    
                    val mimeType = try {
                        context.contentResolver.getType(uri) ?: "video/*"
                    } catch (e: Exception) {
                        "video/*"
                    }
                    
                    return VideoDetails(
                        uri = uri,
                        name = name,
                        duration = duration,
                        width = width,
                        height = height,
                        size = size,
                        mimeType = mimeType
                    ).also {
                        println("DEBUG: Created VideoDetails: $it")
                    }
                }
            }
            return null
        } catch (e: Exception) {
            println("DEBUG: Error in getVideoDetails: ${e.message}")
            e.printStackTrace()
            return null
        }
    }
} 