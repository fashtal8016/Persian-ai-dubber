package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "subtitle_segments",
    foreignKeys = [
        ForeignKey(
            entity = DubbingProject::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class SubtitleSegment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val orderIndex: Int,
    val startMs: Long,
    val endMs: Long,
    val originalText: String,
    val translatedText: String,
    val speaker: String = "Speaker 1",
    val isEditedByUser: Boolean = false,
    val isTtsSynthesized: Boolean = false
) {
    fun formattedStartTime(): String = formatMsToTime(startMs)
    fun formattedEndTime(): String = formatMsToTime(endMs)
    fun durationMs(): Long = (endMs - startMs).coerceAtLeast(0)

    companion object {
        fun formatMsToTime(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val millis = (ms % 1000) / 100
            return String.format("%02d:%02d.%01d", minutes, seconds, millis)
        }

        fun formatMsToSrtTime(ms: Long): String {
            val hours = ms / 3600000
            val minutes = (ms % 3600000) / 60000
            val seconds = (ms % 60000) / 1000
            val millis = ms % 1000
            return String.format("%02d:%02d:%02d,%03d", hours, minutes, seconds, millis)
        }
    }
}
