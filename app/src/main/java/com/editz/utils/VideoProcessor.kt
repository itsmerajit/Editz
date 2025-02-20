package com.editz.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
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
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null
        
        try {
            Log.d(TAG, "Starting video processing: $inputUri")
            extractor = MediaExtractor()
            
            // Set data source
            try {
                extractor.setDataSource(context, inputUri, null)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set data source: ${e.message}")
                throw IllegalStateException("Cannot read video file: ${e.message}")
            }
            
            // Create output directory
            outputFile.parentFile?.mkdirs()
            
            // Create muxer
            muxer = MediaMuxer(
                outputFile.absolutePath,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )
            
            // Process video track
            val videoTrackIndex = findTrackIndex(extractor, "video/")
            if (videoTrackIndex == -1) {
                throw IllegalStateException("No video track found in the file")
            }
            
            Log.d(TAG, "Found video track at index: $videoTrackIndex")
            
            // Get video format and add track to muxer
            val videoFormat = extractor.getTrackFormat(videoTrackIndex)
            videoFormat.setInteger(MediaFormat.KEY_ROTATION, rotation)
            val outputVideoTrack = muxer.addTrack(videoFormat)
            
            // Process audio track if exists
            val audioTrackIndex = findTrackIndex(extractor, "audio/")
            var outputAudioTrack = -1
            
            if (audioTrackIndex != -1) {
                try {
                    Log.d(TAG, "Found audio track at index: $audioTrackIndex")
                    val audioFormat = extractor.getTrackFormat(audioTrackIndex)
                    
                    // Ensure audio format has required keys
                    if (!audioFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                        audioFormat.setInteger(MediaFormat.KEY_CHANNEL_COUNT, 2)
                    }
                    if (!audioFormat.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                        audioFormat.setInteger(MediaFormat.KEY_SAMPLE_RATE, 44100)
                    }
                    if (!audioFormat.containsKey(MediaFormat.KEY_AAC_PROFILE)) {
                        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, 2) // AAC LC
                    }
                    
                    outputAudioTrack = muxer.addTrack(audioFormat)
                    Log.d(TAG, "Added audio track to muxer: $outputAudioTrack")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to add audio track: ${e.message}")
                    // Continue without audio if there's an error
                    outputAudioTrack = -1
                }
            } else {
                Log.d(TAG, "No audio track found")
            }
            
            // Start muxing
            muxer.start()
            
            // Process video frames
            Log.d(TAG, "Processing video frames from ${trimStartMs}ms to ${trimEndMs}ms")
            processVideoTrack(
                extractor = extractor,
                trackIndex = videoTrackIndex,
                muxer = muxer,
                outputTrack = outputVideoTrack,
                startMs = trimStartMs,
                endMs = trimEndMs,
                speed = speed,
                filter = filter,
                progressCallback = { progress -> 
                    progressCallback(if (outputAudioTrack != -1) progress * 0.7f else progress)
                }
            )
            
            // Process audio if exists
            if (audioTrackIndex != -1 && outputAudioTrack != -1) {
                try {
                    Log.d(TAG, "Processing audio track")
                    processAudioTrack(
                        extractor = extractor,
                        trackIndex = audioTrackIndex,
                        muxer = muxer,
                        outputTrack = outputAudioTrack,
                        startMs = trimStartMs,
                        endMs = trimEndMs,
                        speed = speed,
                        volume = volume,
                        progressCallback = { progress ->
                            progressCallback(0.7f + (progress * 0.3f))
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing audio, continuing without audio: ${e.message}")
                    // Continue without audio
                }
            }
            
            // Stop and release resources
            try {
                muxer.stop()
                muxer.release()
                extractor.release()
            } catch (e: Exception) {
                Log.e(TAG, "Error while stopping muxer: ${e.message}")
            }
            
            Log.d(TAG, "Video processing completed successfully")
            Result.success(outputFile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing video: ${e.message}", e)
            
            // Clean up resources
            try {
                muxer?.release()
                extractor?.release()
                
                if (outputFile.exists()) {
                    outputFile.delete()
                }
            } catch (cleanupError: Exception) {
                Log.e(TAG, "Error during cleanup: ${cleanupError.message}")
            }
            
            Result.failure(e)
        }
    }
    
    private fun findTrackIndex(extractor: MediaExtractor, mimePrefix: String): Int {
        for (i in 0 until extractor.trackCount) {
            try {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith(mimePrefix) == true) {
                    return i
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting track format at index $i: ${e.message}")
                continue
            }
        }
        return -1
    }
    
    private suspend fun processVideoTrack(
        extractor: MediaExtractor,
        trackIndex: Int,
        muxer: MediaMuxer,
        outputTrack: Int,
        startMs: Long,
        endMs: Long,
        speed: Float,
        filter: VideoFilter,
        progressCallback: (Float) -> Unit
    ) {
        try {
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            
            // Get frame rate and calculate adjusted timing
            val frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE, 30)
            val duration = endMs - startMs
            val totalFrames = (duration / 1000.0 * frameRate).toLong()
            var processedFrames = 0L
            var lastPresentationTimeUs = 0L
            
            // Calculate frame interval and frame dropping for high speeds
            val normalFrameIntervalUs = 1_000_000L / frameRate
            val speedAdjustedIntervalUs = (normalFrameIntervalUs / speed).toLong()
            val framesToSkip = if (speed > 1.0f) (speed - 1.0f).toInt() else 0
            var frameCounter = 0
            
            try {
                // Seek to start position
                extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                
                val bufferInfo = MediaCodec.BufferInfo()
                val maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
                val buffer = ByteBuffer.allocate(maxBufferSize)
                var lastKeyFrameData: ByteArray? = null
                var isKeyFrame = false
                
                while (true) {
                    buffer.clear()
                    val sampleSize = extractor.readSampleData(buffer, 0)
                    if (sampleSize < 0) {
                        Log.d(TAG, "Reached end of video track")
                        break
                    }
                    
                    val sampleTime = extractor.sampleTime
                    if ((sampleTime / 1000) > endMs) {
                        Log.d(TAG, "Reached end time for video")
                        break
                    }
                    
                    isKeyFrame = (extractor.sampleFlags and MediaExtractor.SAMPLE_FLAG_SYNC) != 0
                    
                    // For high speeds, only process key frames and selected frames
                    if (speed > 1.0f) {
                        if (!isKeyFrame && frameCounter % (framesToSkip + 1) != 0) {
                            extractor.advance()
                            frameCounter++
                            continue
                        }
                    }
                    
                    // Store key frame data
                    if (isKeyFrame) {
                        val tempBuffer = ByteArray(sampleSize)
                        buffer.get(tempBuffer)
                        lastKeyFrameData = tempBuffer
                        buffer.position(0)
                    }
                    
                    // Calculate presentation time
                    val adjustedTimeUs = if (processedFrames == 0L) {
                        0L
                    } else {
                        lastPresentationTimeUs + speedAdjustedIntervalUs
                    }
                    lastPresentationTimeUs = adjustedTimeUs
                    
                    // Write frame
                    bufferInfo.apply {
                        this.size = sampleSize
                        this.offset = 0
                        this.presentationTimeUs = adjustedTimeUs
                        this.flags = extractor.sampleFlags
                    }
                    
                    try {
                        muxer.writeSampleData(outputTrack, buffer, bufferInfo)
                    } catch (e: Exception) {
                        if (isKeyFrame || lastKeyFrameData == null) {
                            Log.e(TAG, "Error writing video sample: ${e.message}")
                            throw IllegalStateException("Failed to write video sample: ${e.message}")
                        } else {
                            // If writing fails for a non-key frame, try using the last key frame
                            buffer.clear()
                            buffer.put(lastKeyFrameData)
                            buffer.flip()
                            try {
                                muxer.writeSampleData(outputTrack, buffer, bufferInfo)
                            } catch (e2: Exception) {
                                Log.e(TAG, "Error writing fallback frame: ${e2.message}")
                                throw IllegalStateException("Failed to write video frame: ${e2.message}")
                            }
                        }
                    }
                    
                    // Advance to next frame
                    if (!extractor.advance()) {
                        Log.d(TAG, "No more video samples to process")
                        break
                    }
                    
                    // Update progress
                    processedFrames++
                    frameCounter++
                    progressCallback(processedFrames.toFloat() / totalFrames)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing video frames: ${e.message}", e)
                throw IllegalStateException("Failed to process video frames: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error in video track processing: ${e.message}", e)
            throw IllegalStateException("Failed to process video track: ${e.message}")
        }
    }
    
    private suspend fun processAudioTrack(
        extractor: MediaExtractor,
        trackIndex: Int,
        muxer: MediaMuxer,
        outputTrack: Int,
        startMs: Long,
        endMs: Long,
        speed: Float,
        volume: Float,
        progressCallback: (Float) -> Unit
    ) {
        try {
            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            
            // Get sample rate and calculate adjusted timing
            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE, 44100)
            val duration = endMs - startMs
            var lastPresentationTimeUs = 0L
            
            // Calculate sample interval based on speed
            val sampleIntervalUs = (1_000_000f / (sampleRate * speed)).toLong()
            
            // Create buffer for reading samples
            val maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 1024 * 1024)
            val buffer = ByteBuffer.allocate(maxBufferSize)
            val bufferInfo = MediaCodec.BufferInfo()
            
            // Seek to start position
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            
            var totalBytes = 0L
            val estimatedBytes = (duration / 1000.0 * sampleRate * 2).toLong() // Rough estimate
            
            while (true) {
                val sampleSize = extractor.readSampleData(buffer, 0)
                if (sampleSize < 0) {
                    Log.d(TAG, "Reached end of audio track")
                    break
                }
                
                val sampleTime = extractor.sampleTime
                if ((sampleTime / 1000) > endMs) {
                    Log.d(TAG, "Reached end time for audio")
                    break
                }
                
                // Calculate new presentation time based on speed
                val adjustedTimeUs = if (totalBytes == 0L) {
                    0L // First sample starts at 0
                } else {
                    lastPresentationTimeUs + sampleIntervalUs
                }
                lastPresentationTimeUs = adjustedTimeUs
                
                // Apply volume adjustment if needed
                if (volume != 1f) {
                    adjustVolume(buffer, volume)
                }
                
                // Write audio sample with adjusted timing
                bufferInfo.apply {
                    this.size = sampleSize
                    this.offset = 0
                    this.presentationTimeUs = adjustedTimeUs
                    this.flags = extractor.sampleFlags
                }
                
                try {
                    muxer.writeSampleData(outputTrack, buffer, bufferInfo)
                    totalBytes += sampleSize
                    
                    // Update progress
                    val progress = totalBytes.toFloat() / estimatedBytes
                    progressCallback(progress.coerceIn(0f, 1f))
                } catch (e: Exception) {
                    Log.e(TAG, "Error writing audio sample: ${e.message}")
                    throw IllegalStateException("Failed to write audio sample: ${e.message}")
                }
                
                if (!extractor.advance()) {
                    Log.d(TAG, "No more audio samples to process")
                    break
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing audio track: ${e.message}", e)
            throw IllegalStateException("Failed to process audio track: ${e.message}")
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
    
    private fun applyFilter(buffer: ByteBuffer, filter: VideoFilter) {
        // Basic filter implementation
        when (filter) {
            VideoFilter.ORIGINAL -> return
            else -> {
                // For now, we'll just pass through
                // TODO: Implement actual filters
            }
        }
    }
    
    companion object {
        private const val TAG = "VideoProcessor"
        private const val TIMEOUT_US = 10000L
    }
} 