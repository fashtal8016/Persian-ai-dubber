package com.example.domain.service

import android.content.Context
import com.example.data.model.SubtitleSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class MediaProcessingService(private val context: Context) {

    /**
     * Converts a list of subtitle segments into standard SRT format.
     */
    fun generateSrtContent(segments: List<SubtitleSegment>): String {
        val sb = StringBuilder()
        segments.sortedBy { it.orderIndex }.forEachIndexed { idx, segment ->
            sb.append("${idx + 1}\n")
            sb.append("${SubtitleSegment.formatMsToSrtTime(segment.startMs)} --> ${SubtitleSegment.formatMsToSrtTime(segment.endMs)}\n")
            sb.append("${segment.translatedText}\n\n")
        }
        return sb.toString().trim()
    }

    /**
     * Saves SRT subtitle to app external files or cache.
     */
    suspend fun saveSrtFile(projectId: Long, srtContent: String): File = withContext(Dispatchers.IO) {
        val file = File(context.getExternalFilesDir(null) ?: context.filesDir, "subtitles_project_${projectId}.srt")
        FileOutputStream(file).use {
            it.write(srtContent.toByteArray(Charsets.UTF_8))
        }
        file
    }

    companion object {
        // High quality standard MP4 video streams for instant demo and playback
        const val SAMPLE_VIDEO_AI_REVOLUTION = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        const val SAMPLE_VIDEO_NATURE = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
        const val SAMPLE_VIDEO_TECH = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
    }
}
