package com.editz.data.manager

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoSaveManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    sealed class SaveResult {
        object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
        object InProgress : SaveResult()
        data class Progress(val progress: Float) : SaveResult()
    }

    fun saveVideo(
        sourceFilePath: String,
        fileName: String,
        mimeType: String = "video/mp4"
    ): Flow<SaveResult> = channelFlow {
        send(SaveResult.InProgress)
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveVideoApi29Plus(sourceFilePath, fileName, mimeType) { progress ->
                    send(SaveResult.Progress(progress))
                }
            } else {
                saveVideoLegacy(sourceFilePath, fileName) { progress ->
                    send(SaveResult.Progress(progress))
                }
            }.let { result ->
                send(result)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video: ${e.message}", e)
            send(SaveResult.Error("Failed to save video: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun saveVideoApi29Plus(
        sourceFilePath: String,
        fileName: String,
        mimeType: String,
        onProgress: suspend (Float) -> Unit
    ): SaveResult {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
            put(MediaStore.Video.Media.IS_PENDING, 1)
            put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            put(MediaStore.Video.Media.TITLE, fileName.substringBeforeLast("."))
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return SaveResult.Error("Failed to create MediaStore entry")

        return try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                copyFileWithProgress(File(sourceFilePath), outputStream, onProgress)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            contentValues.put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            resolver.update(uri, contentValues, null, null)

            // Notify media scanner
            context.sendBroadcast(android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))

            SaveResult.Success
        } catch (e: IOException) {
            resolver.delete(uri, null, null)
            Log.e(TAG, "Error saving video: ${e.message}", e)
            SaveResult.Error("Failed to save video: ${e.message}")
        }
    }

    private suspend fun saveVideoLegacy(
        sourceFilePath: String,
        fileName: String,
        onProgress: suspend (Float) -> Unit
    ): SaveResult {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        if (!moviesDir.exists()) {
            moviesDir.mkdirs()
        }
        
        val destinationFile = File(moviesDir, fileName)

        return try {
            FileInputStream(File(sourceFilePath)).use { input ->
                destinationFile.outputStream().use { output ->
                    copyFileWithProgress(File(sourceFilePath), output, onProgress)
                }
            }

            // Scan the file so it appears in gallery immediately
            MediaScannerConnection.scanFile(
                context,
                arrayOf(destinationFile.absolutePath),
                arrayOf("video/mp4")
            ) { path, uri ->
                Log.d(TAG, "Media scan completed. Path: $path, Uri: $uri")
            }

            // Add to MediaStore
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.DATA, destinationFile.absolutePath)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.TITLE, fileName.substringBeforeLast("."))
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.DATE_MODIFIED, System.currentTimeMillis() / 1000)
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)

            SaveResult.Success
        } catch (e: IOException) {
            Log.e(TAG, "Error saving video: ${e.message}", e)
            SaveResult.Error("Failed to save video: ${e.message}")
        }
    }

    private suspend fun copyFileWithProgress(
        sourceFile: File,
        outputStream: OutputStream,
        onProgress: suspend (Float) -> Unit
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val sourceSize = sourceFile.length()
        var bytesCopied = 0L
        
        FileInputStream(sourceFile).use { input ->
            var bytes = input.read(buffer)
            while (bytes >= 0) {
                outputStream.write(buffer, 0, bytes)
                bytesCopied += bytes
                val progress = bytesCopied.toFloat() / sourceSize.toFloat()
                onProgress(progress)
                bytes = input.read(buffer)
            }
        }
    }

    companion object {
        private const val TAG = "VideoSaveManager"
        private const val DEFAULT_BUFFER_SIZE = 8192 // 8KB buffer
    }
} 