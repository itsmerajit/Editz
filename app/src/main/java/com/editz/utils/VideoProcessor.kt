package com.editz.utils

import android.content.Context
import android.media.MediaCodec
import android.media.MediaCodecInfo
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
    companion object {
        private const val TAG = "VideoProcessor"
        private const val TIMEOUT_US = 10000L
        
        // Color format constants
        private const val COLOR_FormatYUV420Planar = 19
        private const val COLOR_FormatYUV420PackedPlanar = 20
        private const val COLOR_FormatYUV420SemiPlanar = 21
        private const val COLOR_FormatYUV420PackedSemiPlanar = 39
        
        // Profile constants
        private const val AVC_PROFILE_BASELINE = 0x01
        private const val AVC_LEVEL_31 = 0x10
    }
    
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
        var decoder: MediaCodec? = null
        var encoder: MediaCodec? = null
        
        try {
            Log.d(TAG, "Starting video track processing: trackIndex=$trackIndex, startMs=$startMs, endMs=$endMs, speed=$speed")
            extractor.selectTrack(trackIndex)
            val inputFormat = extractor.getTrackFormat(trackIndex)
            Log.d(TAG, "Input format: $inputFormat")
            
            // Validate video format before processing
            if (!inputFormat.containsKey(MediaFormat.KEY_MIME)) {
                throw IllegalStateException("Invalid video format: Missing MIME type")
            }

            // Get format parameters with defaults
            val width = inputFormat.getInteger(MediaFormat.KEY_WIDTH)
            val height = inputFormat.getInteger(MediaFormat.KEY_HEIGHT)
            val bitRate = try {
                inputFormat.getInteger(MediaFormat.KEY_BIT_RATE)
            } catch (e: Exception) {
                width * height * 4  // Default bitrate based on resolution
            }
            val frameRate = try {
                inputFormat.getInteger(MediaFormat.KEY_FRAME_RATE)
            } catch (e: Exception) {
                30  // Default frame rate
            }
            
            Log.d(TAG, "Video parameters: width=$width, height=$height, bitRate=$bitRate, frameRate=$frameRate")
            
            // Configure decoder
            val mime = inputFormat.getString(MediaFormat.KEY_MIME)!!
            Log.d(TAG, "Creating decoder for mime type: $mime")
            decoder = MediaCodec.createDecoderByType(mime)
            
            // Set color aspects before configuring decoder
            inputFormat.setInteger(MediaFormat.KEY_COLOR_STANDARD, MediaCodecInfo.CodecCapabilities.COLOR_STANDARD_BT709)
            inputFormat.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaCodecInfo.CodecCapabilities.COLOR_RANGE_FULL)
            inputFormat.setInteger(MediaFormat.KEY_COLOR_TRANSFER, MediaCodecInfo.CodecCapabilities.COLOR_TRANSFER_SDR_VIDEO)
            
            decoder.configure(inputFormat, null, null, 0)
            decoder.start()
            Log.d(TAG, "Decoder configured and started with format: $inputFormat")
            
            // Configure encoder with compatible format
            Log.d(TAG, "Creating encoder for mime type: $mime")
            encoder = MediaCodec.createEncoderByType(mime)
            
            // Create a new format instead of modifying the input format
            val outputFormat = MediaFormat.createVideoFormat(mime, width, height).apply {
                // Essential parameters
                setInteger(MediaFormat.KEY_WIDTH, width)
                setInteger(MediaFormat.KEY_HEIGHT, height)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                
                // Copy color aspects from input format
                setInteger(MediaFormat.KEY_COLOR_STANDARD, MediaCodecInfo.CodecCapabilities.COLOR_STANDARD_BT709)
                setInteger(MediaFormat.KEY_COLOR_RANGE, MediaCodecInfo.CodecCapabilities.COLOR_RANGE_FULL)
                setInteger(MediaFormat.KEY_COLOR_TRANSFER, MediaCodecInfo.CodecCapabilities.COLOR_TRANSFER_SDR_VIDEO)
                
                // Use default color format for encoder with proper null checks
                val selectedColorFormat = try {
                    val codecInfo = encoder.codecInfo
                    if (codecInfo == null) {
                        Log.e(TAG, "Encoder codec info is null!")
                        throw IllegalStateException("Encoder codec info is null")
                    }
                    
                    val capabilities = codecInfo.getCapabilitiesForType(mime)
                    if (capabilities == null) {
                        Log.e(TAG, "Encoder capabilities are null!")
                        throw IllegalStateException("Encoder capabilities are null")
                    }
                    
                    val colorFormats = capabilities.colorFormats
                    Log.d(TAG, "Available encoder color formats: ${colorFormats.joinToString()}")
                    
                    colorFormats.find { format ->
                        format == COLOR_FormatYUV420SemiPlanar
                    } ?: colorFormats.firstOrNull() ?: throw IllegalStateException("No supported color formats found")
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error selecting color format: ${e.message}")
                    COLOR_FormatYUV420SemiPlanar
                }
                
                Log.d(TAG, "Setting encoder color format to: $selectedColorFormat")
                setInteger(MediaFormat.KEY_COLOR_FORMAT, selectedColorFormat)
                
                // Set profile and level for better compatibility
                if (mime.contains("avc", ignoreCase = true)) {
                    setInteger(MediaFormat.KEY_PROFILE, AVC_PROFILE_BASELINE)
                    setInteger(MediaFormat.KEY_LEVEL, AVC_LEVEL_31)
                }
            }
            
            Log.d(TAG, "Final output format: $outputFormat")
            
            try {
                encoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to configure encoder: ${e.message}")
                throw IllegalStateException("Failed to configure encoder: ${e.message}")
            }
            
            encoder.start()
            Log.d(TAG, "Encoder configured and started")
            
            val duration = endMs - startMs
            val totalFrames = (duration / 1000.0 * frameRate).toLong()
            var processedFrames = 0L
            
            Log.d(TAG, "Calculated frames: duration=${duration}ms, totalFrames=$totalFrames")
            
            // Seek to start position
            extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
            Log.d(TAG, "Extractor seeked to position: ${startMs}ms")
            
            val info = MediaCodec.BufferInfo()
            val timeoutUs = 10000L
            var inputDone = false
            var outputDone = false
            var hasCodecSpecificData = false
            
            while (!outputDone) {
                if (!inputDone) {
                    val inputBufferId = decoder.dequeueInputBuffer(timeoutUs)
                    if (inputBufferId >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputBufferId)
                        
                        // Check for corrupt frames
                        if (inputBuffer == null || inputBuffer.remaining() == 0) {
                            Log.w(TAG, "Skipping corrupt video frame")
                            continue
                        }
                        
                        val sampleSize = extractor.readSampleData(inputBuffer, 0)
                        val sampleTime = extractor.sampleTime
                        
                        when {
                            sampleSize < 0 -> {
                                Log.d(TAG, "Reached end of input stream")
                                decoder.queueInputBuffer(inputBufferId, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            }
                            (sampleTime / 1000) > endMs -> {
                                Log.d(TAG, "Reached end time: current=${sampleTime/1000}ms, target=${endMs}ms")
                                decoder.queueInputBuffer(inputBufferId, 0, 0, 0L,
                                    MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                                inputDone = true
                            }
                            else -> {
                                val presentationTimeUs = (sampleTime / speed).toLong()
                                decoder.queueInputBuffer(inputBufferId, 0, sampleSize,
                                    presentationTimeUs, extractor.sampleFlags)
                                Log.v(TAG, "Queued input buffer: size=$sampleSize, time=${presentationTimeUs}us")
                                extractor.advance()
                            }
                        }
                    }
                }
                
                // Handle encoder output
                val encoderOutputId = encoder.dequeueOutputBuffer(info, timeoutUs)
                when {
                    encoderOutputId >= 0 -> {
                        val encodedData = encoder.getOutputBuffer(encoderOutputId)!!
                        if (info.size > 0) {
                            // Check for codec-specific data
                            if ((info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                                if (!hasCodecSpecificData) {
                                    Log.d(TAG, "Writing codec specific data")
                                    muxer.writeSampleData(outputTrack, encodedData, info)
                                    hasCodecSpecificData = true
                                } else {
                                    Log.d(TAG, "Skipping duplicate codec specific data")
                                }
                            } else {
                                Log.v(TAG, "Writing encoded data: size=${info.size}, time=${info.presentationTimeUs}us")
                                muxer.writeSampleData(outputTrack, encodedData, info)
                            }
                        }
                        encoder.releaseOutputBuffer(encoderOutputId, false)
                        if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            Log.d(TAG, "Encoder signaled end of stream")
                            outputDone = true
                        }
                    }
                    encoderOutputId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED -> {
                        val newFormat = encoder.getOutputFormat()
                        Log.d(TAG, "Encoder output format changed: $newFormat")
                    }
                    encoderOutputId == MediaCodec.INFO_TRY_AGAIN_LATER -> {
                        Log.v(TAG, "Encoder output not available yet")
                    }
                }
            }
            
            Log.d(TAG, "Video track processing completed successfully: processed $processedFrames frames")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing video track", e)
            Log.e(TAG, "Stack trace: ${e.stackTrace.joinToString("\n")}")
            Log.e(TAG, "Cause: ${e.cause?.message ?: "Unknown"}")
            throw IllegalStateException("Failed to process video track: ${e.message}", e)
        } finally {
            try {
                Log.d(TAG, "Cleaning up codec resources")
                decoder?.stop()
                decoder?.release()
                encoder?.stop()
                encoder?.release()
                Log.d(TAG, "Codec resources released")
            } catch (e: Exception) {
                Log.e(TAG, "Error releasing codecs", e)
            }
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
}