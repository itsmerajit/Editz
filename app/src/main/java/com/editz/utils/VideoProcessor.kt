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
        rotation: Int
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val extractor = MediaExtractor().apply {
                setDataSource(context, inputUri, null)
            }
            
            val muxer = MediaMuxer(
                outputFile.absolutePath,
                MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4
            )
            
            // Process video track
            val videoTrackIndex = findTrackIndex(extractor, "video/")
            val videoFormat = extractor.getTrackFormat(videoTrackIndex)
            val processedVideoFormat = videoFormat.apply {
                setInteger(
                    MediaFormat.KEY_ROTATION,
                    rotation
                )
            }
            
            val outputVideoTrack = muxer.addTrack(processedVideoFormat)
            
            // Process audio track
            val audioTrackIndex = findTrackIndex(extractor, "audio/")
            val audioFormat = extractor.getTrackFormat(audioTrackIndex)
            val outputAudioTrack = muxer.addTrack(audioFormat)
            
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
                filter = filter
            )
            
            // Process audio samples
            processTrack(
                extractor = extractor,
                trackIndex = audioTrackIndex,
                muxer = muxer,
                outputTrack = outputAudioTrack,
                startMs = trimStartMs,
                endMs = trimEndMs,
                speed = speed,
                volume = volume
            )
            
            muxer.stop()
            muxer.release()
            extractor.release()
            
            Result.success(outputFile)
        } catch (e: Exception) {
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
        throw IllegalArgumentException("Track not found for mime prefix: $mimePrefix")
    }
    
    private fun processTrack(
        extractor: MediaExtractor,
        trackIndex: Int,
        muxer: MediaMuxer,
        outputTrack: Int,
        startMs: Long,
        endMs: Long,
        speed: Float = 1f,
        volume: Float = 1f,
        filter: VideoFilter? = null
    ) {
        val format = extractor.getTrackFormat(trackIndex)
        val mime = format.getString(MediaFormat.KEY_MIME)
        val decoder = MediaCodec.createDecoderByType(mime!!)
        val encoder = MediaCodec.createEncoderByType(mime)
        
        decoder.configure(format, null, null, 0)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        
        decoder.start()
        encoder.start()
        
        val bufferInfo = MediaCodec.BufferInfo()
        val maxBufferSize = format.getInteger(MediaFormat.KEY_MAX_INPUT_SIZE)
        val inputBuffer = ByteBuffer.allocate(maxBufferSize)
        
        extractor.selectTrack(trackIndex)
        extractor.seekTo(startMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)
        
        var isEOS = false
        while (!isEOS) {
            val inputBufferId = decoder.dequeueInputBuffer(TIMEOUT_US)
            if (inputBufferId >= 0) {
                val inputBuffer = decoder.getInputBuffer(inputBufferId)
                val sampleSize = extractor.readSampleData(inputBuffer!!, 0)
                
                when {
                    sampleSize < 0 -> {
                        decoder.queueInputBuffer(
                            inputBufferId, 0, 0,
                            0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEOS = true
                    }
                    extractor.sampleTime / 1000 > endMs -> {
                        decoder.queueInputBuffer(
                            inputBufferId, 0, 0,
                            0, MediaCodec.BUFFER_FLAG_END_OF_STREAM
                        )
                        isEOS = true
                    }
                    else -> {
                        decoder.queueInputBuffer(
                            inputBufferId, 0, sampleSize,
                            extractor.sampleTime, 0
                        )
                        extractor.advance()
                    }
                }
            }
            
            val outputBufferId = decoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_US)
            if (outputBufferId >= 0) {
                val outputBuffer = decoder.getOutputBuffer(outputBufferId)
                val presentationTimeUs = (bufferInfo.presentationTimeUs / speed).toLong()
                
                // Apply filter or volume adjustment
                when {
                    filter != null -> applyFilter(outputBuffer!!, filter)
                    volume != 1f -> adjustVolume(outputBuffer!!, volume)
                }
                
                muxer.writeSampleData(outputTrack, outputBuffer!!, bufferInfo)
                decoder.releaseOutputBuffer(outputBufferId, false)
            }
        }
        
        decoder.stop()
        decoder.release()
        encoder.stop()
        encoder.release()
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