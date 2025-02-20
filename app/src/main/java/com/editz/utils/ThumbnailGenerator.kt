package com.editz.utils

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.collection.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailGenerator @Inject constructor(
    private val context: Context
) {
    private val cache = LruCache<String, Bitmap>(50) // Cache up to 50 thumbnails
    
    suspend fun generateThumbnail(
        uri: Uri,
        timeMs: Long,
        width: Int = 100,
        height: Int = 100
    ): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${uri}_${timeMs}_${width}x${height}"
        
        // Check cache first
        cache.get(cacheKey)?.let { return@withContext it }
        
        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                
                // Get frame
                retriever.getFrameAtTime(
                    timeMs * 1000, // Convert to microseconds
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )?.let { frame ->
                    // Scale bitmap
                    val scaled = Bitmap.createScaledBitmap(
                        frame,
                        width,
                        height,
                        true
                    )
                    
                    // Cache the result
                    cache.put(cacheKey, scaled)
                    
                    scaled
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun generateThumbnailStrip(
        uri: Uri,
        duration: Long,
        numThumbnails: Int
    ): List<Bitmap> = withContext(Dispatchers.IO) {
        val thumbnails = mutableListOf<Bitmap>()
        val interval = duration / numThumbnails

        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, uri)
                
                for (i in 0 until numThumbnails) {
                    val timeUs = ((i.toFloat() / numThumbnails.toFloat()) * duration).toLong()
                    val cacheKey = "${uri}_$timeUs"
                    
                    // Try to get from cache first
                    var thumbnail = cache.get(cacheKey)
                    
                    if (thumbnail == null) {
                        // Generate thumbnail if not in cache
                        thumbnail = retriever.getFrameAtTime(
                            timeUs * 1000, // Convert to microseconds
                            MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                        )
                        
                        // Cache the thumbnail
                        if (thumbnail != null) {
                            cache.put(cacheKey, thumbnail)
                        }
                    }
                    
                    thumbnail?.let { thumbnails.add(it) }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        thumbnails
    }
    
    fun clearCache() {
        cache.evictAll()
    }
} 