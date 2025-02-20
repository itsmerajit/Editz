package com.editz.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import com.editz.ui.editor.model.VideoFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.ByteBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    suspend fun processVideo(
        inputUri: Uri,
        outputFile: File,
        trimStartMs: Long,
        trimEndMs: Long,
        filter: VideoFilter,
        speed: Float,
        volume: Float,
        rotation: Int,
        progressCallback: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor()
            try {
                extractor.setDataSource(context, inputUri, null)
                
                // Create output directory if it doesn't exist
                outputFile.parentFile?.mkdirs()
                
                val muxer = MediaMuxer(
                    outputFile.absolutePath,
                    MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
                )
                
                // Find and process video track
                val videoTrackIndex = findTrackIndex(extractor, "video/")
                if (videoTrackIndex == -1) {
                    return@withContext Result.failure(IllegalStateException("No video track found"))
                }
                
                val videoFormat = extractor.getTrackFormat(videoTrackIndex)
                val processedVideoFormat = videoFormat.apply {
                    setInteger(MediaFormat.KEY_ROTATION, rotation)
                }
                
                val outputVideoTrack = muxer.addTrack(processedVideoFormat)
                
                // Find and process audio track if exists
                val audioTrackIndex = findTrackIndex(extractor, "audio/")
                val outputAudioTrack = if (audioTrackIndex != -1) {
                    val audioFormat = extractor.getTrackFormat(audioTrackIndex)
                    muxer.addTrack(audioFormat)
                } else {
                    -1
                }
                
                muxer.start()
                
                // Process video frames
                processTrack(
                    extractor = extractor,
                    trackIndex = videoTrackIndex,
                    muxer = muxer,
                    outputTrack = outputVideoTrack,
                    startMs = trimStartMs,
                    endMs = trimEndMs,
                    speed = speed,
                    filter = filter,
                    progressCallback = { progress -> 
                        progressCallback(progress * 0.7f) // Video is 70% of progress
                    }
                )
                
                // Process audio if exists
                if (audioTrackIndex != -1 && outputAudioTrack != -1) {
                    processTrack(
                        extractor = extractor,
                        trackIndex = audioTrackIndex,
                        muxer = muxer,
                        outputTrack = outputAudioTrack,
                        startMs = trimStartMs,
                        endMs = trimEndMs,
                        speed = speed,
                        volume = volume,
                        progressCallback = { progress ->
                            progressCallback(0.7f + (progress * 0.3f)) // Audio is 30% of progress
                        }
                    )
                }
                
                muxer.stop()
                muxer.release()
                extractor.release()
                
                Result.success(outputFile)
            } catch (e: Exception) {
                extractor.release()
                throw e
            }
        } catch (e: Exception) {
            // Clean up output file if it exists
            if (outputFile.exists()) {
                outputFile.delete()
            }
            Result.failure(e)
        }
    }
    
    private fun findTrackIndex(extractor: MediaExtractor, mimePrefix: String): Int {
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            if (format.getString(MediaFormat.KEY_MIME)?.startsWith(mimePrefix) == true) {
                return i
            }
        }
        return -1 // Return -1 if track not found
    }
    
    private suspend fun processTrack(
        extractor: MediaExtractor,
        trackIndex: Int,
        muxer: MediaMuxer,
        outputTrack: Int,
        startMs: Long,
        endMs: Long,
        speed: Float = 1f,
        volume: Float = 1f,
        filter: VideoFilter? = null,
        progressCallback: (Float) -> Unit = {}
    ) {
        extractor.selectTrack(trackIndex)
        val format = extractor.getTrackFormat(trackIndex)
        
        // Calculate total frames/samples for progress
        val duration = endMs - startMs
        val totalBytes = (format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0) * 
            (duration / 1000.0 * format.getInteger(MediaFormat.KEY_FRAME_RATE, 30))).toLong()
        var processedBytes = 0L
        
        try {
            // Seek to start position
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            
            val bufferInfo = MediaCodec.BufferInfo()
            val maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
            val buffer = ByteBuffer.allocate(maxBufferSize)
            
            // Process frames/samples
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                val sampleTime = extractor.sampleTime
                
                if (sampleSize < 0 || (sampleTime / 1000) > endMs) {
                    break
                }
                
                // Apply effects
                if (filter != null) {
                    applyFilter(buffer, filter)
                }
                if (volume != 1f) {
                    adjustVolume(buffer, volume)
                }
                
                // Write frame/sample
                bufferInfo.apply {
                    this.size = sampleSize
                    this.offset = 0
                    this.presentationTimeUs = ((sampleTime - startMs * 1000) / speed).toLong()
                    this.flags = extractor.sampleFlags
                }
                
                muxer.writeSampleData(outputTrack, buffer, bufferInfo)
                extractor.advance()
                
                // Update progress
                processedBytes += sampleSize
                progressCallback(processedBytes.toFloat() / totalBytes)
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to process track: ${e.message}", e)
        }
    }
    
    private fun applyFilter(buffer: ByteBuffer, filter: VideoFilter) {
        when (filter) {
            VideoFilter.ORIGINAL -> return
            VideoFilter.VINTAGE -> applyVintageFilter(buffer)
            VideoFilter.DRAMATIC -> applyDramaticFilter(buffer)
            VideoFilter.COOL -> applyCoolFilter(buffer)
            VideoFilter.WARM -> applyWarmFilter(buffer)
            VideoFilter.VIBRANT -> applyVibrantFilter(buffer)
            VideoFilter.MUTED -> applyMutedFilter(buffer)
        }
    }
    
    private fun adjustVolume(buffer: ByteBuffer, volume: Float) {
        if (volume == 1f) return
        
        val array = ByteArray(buffer.remaining())
        buffer.get(array)
        
        for (i in array.indices step 2) {
            val sample = (array[i].toInt() and 0xFF) or (array[i + 1].toInt() shl 8)
            val adjustedSample = (sample * volume).toInt().coerceIn(-32768, 32767)
            array[i] = adjustedSample.toByte()
            array[i + 1] = (adjustedSample shr 8).toByte()
        }
        
        buffer.clear()
        buffer.put(array)
        buffer.flip()
    }
    
    private fun applyVintageFilter(buffer: ByteBuffer) {
        // TODO: Implement vintage filter
    }
    
    private fun applyDramaticFilter(buffer: ByteBuffer) {
        // TODO: Implement dramatic filter
    }
    
    private fun applyCoolFilter(buffer: ByteBuffer) {
        // TODO: Implement cool filter
    }
    
    private fun applyWarmFilter(buffer: ByteBuffer) {
        // TODO: Implement warm filter
    }
    
    private fun applyVibrantFilter(buffer: ByteBuffer) {
        // TODO: Implement vibrant filter
    }
    
    private fun applyMutedFilter(buffer: ByteBuffer) {
        // TODO: Implement muted filter
    }
    
    companion object {
        private const val TIMEOUT_US = 10000L
    }
} 