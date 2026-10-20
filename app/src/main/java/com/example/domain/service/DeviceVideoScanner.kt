package com.example.domain.service

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class DeviceVideoItem(
    val id: Long,
    val uriString: String,
    val title: String,
    val displayName: String,
    val durationMs: Long,
    val sizeBytes: Long,
    val resolution: String,
    val isBundledDemo: Boolean = false
) {
    fun formattedDuration(): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    fun formattedSize(): String {
        val mb = sizeBytes / (1024f * 1024f)
        return String.format("%.1f MB", mb)
    }
}

class DeviceVideoScanner(private val context: Context) {

    /**
     * Scans all video files available on the phone via MediaStore.
     * Also merges high-quality sample video clips for seamless testing in emulators.
     */
    suspend fun scanAllDeviceVideos(): List<DeviceVideoItem> = withContext(Dispatchers.IO) {
        val videoList = mutableListOf<DeviceVideoItem>()

        // 1. Query MediaStore for real on-device videos
        try {
            val projection = arrayOf(
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.SIZE,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.RESOLUTION else MediaStore.Video.Media._ID
            )

            val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
                val resolutionColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION)
                } else -1

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn) ?: "Video_$id.mp4"
                    val title = cursor.getString(titleColumn) ?: name
                    val duration = cursor.getLong(durationColumn)
                    val size = cursor.getLong(sizeColumn)
                    val resolution = if (resolutionColumn != -1) cursor.getString(resolutionColumn) ?: "1080p" else "1080p"

                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    videoList.add(
                        DeviceVideoItem(
                            id = id,
                            uriString = contentUri.toString(),
                            title = title,
                            displayName = name,
                            durationMs = if (duration > 0) duration else 25000L,
                            sizeBytes = if (size > 0) size else 15 * 1024 * 1024L,
                            resolution = resolution,
                            isBundledDemo = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Permission or security exception fallback
        }

        // 2. Always provide rich video presets so emulator never appears empty!
        val bundledDemoVideos = listOf(
            DeviceVideoItem(
                id = 9001L,
                uriString = MediaProcessingService.SAMPLE_VIDEO_AI_REVOLUTION,
                title = "هوش مصنوعی و پردازش عصبی (AI Revolution)",
                displayName = "AI_Revolution_Keynote_2026.mp4",
                durationMs = 26500L,
                sizeBytes = 28 * 1024 * 1024L,
                resolution = "1920x1080 (Full HD)",
                isBundledDemo = true
            ),
            DeviceVideoItem(
                id = 9002L,
                uriString = MediaProcessingService.SAMPLE_VIDEO_TECH,
                title = "محاسبات کوانتومی و تراشه‌های فیوژن (Quantum Tech)",
                displayName = "Quantum_Computing_Future.mp4",
                durationMs = 22000L,
                sizeBytes = 19 * 1024 * 1024L,
                resolution = "1920x1080 (Full HD)",
                isBundledDemo = true
            ),
            DeviceVideoItem(
                id = 9003L,
                uriString = MediaProcessingService.SAMPLE_VIDEO_NATURE,
                title = "مستند حیات وحش و اکوسیستم زمین (Nature Wild)",
                displayName = "Wild_Planet_Documentary.mp4",
                durationMs = 35000L,
                sizeBytes = 42 * 1024 * 1024L,
                resolution = "3840x2160 (4K)",
                isBundledDemo = true
            )
        )

        // Return combined list with device videos first
        videoList + bundledDemoVideos
    }
}
