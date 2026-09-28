package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.util.Locale

object VideoUtils {

    data class VideoMeta(
        val fileName: String,
        val durationMs: Long,
        val sizeBytes: Long,
        val thumbnail: Bitmap? = null,
        val thumbnailBase64: String? = null
    )

    fun extractMetadata(context: Context, uri: Uri): VideoMeta {
        var name = "video_${System.currentTimeMillis()}.mp4"
        var size: Long = 0

        // Query content resolver for display name and size
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (cursor.moveToFirst()) {
                    if (nameIndex != -1) name = cursor.getString(nameIndex) ?: name
                    if (sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            }
        } catch (_: Exception) {}

        var durationMs: Long = 0
        var thumbnail: Bitmap? = null
        var base64Thumb: String? = null

        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, uri)
            val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            if (!durStr.isNullOrBlank()) {
                durationMs = durStr.toLongOrNull() ?: 0L
            }
            // Capture a frame at 1 second or first available
            thumbnail = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.frameAtTime

            if (thumbnail != null) {
                // Scale down slightly for performance and base64 transmission
                val scaled = if (thumbnail.width > 720) {
                    val ratio = 720f / thumbnail.width
                    Bitmap.createScaledBitmap(thumbnail, 720, (thumbnail.height * ratio).toInt(), true)
                } else thumbnail

                val baos = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 75, baos)
                val bytes = baos.toByteArray()
                base64Thumb = Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (_: Exception) {
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        return VideoMeta(
            fileName = name,
            durationMs = durationMs,
            sizeBytes = size,
            thumbnail = thumbnail,
            thumbnailBase64 = base64Thumb
        )
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "--:--"
        val totalSecs = durationMs / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "Tamanho desconhecido"
        val mb = bytes / (1024f * 1024f)
        return String.format(Locale.getDefault(), "%.1f MB", mb)
    }

    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        return (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) &&
                trimmed.length > 8 && trimmed.contains(".")
    }
}
