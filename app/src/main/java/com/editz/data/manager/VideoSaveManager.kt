package com.editz.data.manager

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
    ): Flow<SaveResult> = flow {
        emit(SaveResult.InProgress)
        
        try {
            val result = withContext(Dispatchers.IO) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveVideoApi29Plus(sourceFilePath, fileName, mimeType) { progress ->
                        emit(SaveResult.Progress(progress))
                    }
                } else {
                    saveVideoLegacy(sourceFilePath, fileName) { progress ->
                        emit(SaveResult.Progress(progress))
                    }
                }
            }
            emit(result)
        } catch (e: Exception) {
            emit(SaveResult.Error("Failed to save video: ${e.localizedMessage}"))
        }
    }

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
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: return SaveResult.Error("Failed to create MediaStore entry")

        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                copyFileWithProgress(File(sourceFilePath), outputStream, onProgress)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            return SaveResult.Success
        } catch (e: IOException) {
            resolver.delete(uri, null, null)
            return SaveResult.Error("Failed to save video: ${e.localizedMessage}")
        }
    }

    private suspend fun saveVideoLegacy(
        sourceFilePath: String,
        fileName: String,
        onProgress: suspend (Float) -> Unit
    ): SaveResult {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val destinationFile = File(moviesDir, fileName)

        return try {
            FileInputStream(File(sourceFilePath)).use { input ->
                destinationFile.outputStream().use { output ->
                    copyFileWithProgress(File(sourceFilePath), output, onProgress)
                }
            }
            SaveResult.Success
        } catch (e: IOException) {
            SaveResult.Error("Failed to save video: ${e.localizedMessage}")
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
        private const val DEFAULT_BUFFER_SIZE = 8192 // 8KB buffer
    }
} 