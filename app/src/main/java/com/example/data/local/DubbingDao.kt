package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DubbingProject
import com.example.data.model.ProjectStatus
import com.example.data.model.SubtitleSegment
import kotlinx.coroutines.flow.Flow

@Dao
interface DubbingDao {

    // --- Projects Queries ---
    @Query("SELECT * FROM dubbing_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<DubbingProject>>

    @Query("SELECT * FROM dubbing_projects WHERE id = :id")
    fun getProjectById(id: Long): Flow<DubbingProject?>

    @Query("SELECT * FROM dubbing_projects WHERE id = :id")
    suspend fun getProjectByIdOnce(id: Long): DubbingProject?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: DubbingProject): Long

    @Update
    suspend fun updateProject(project: DubbingProject)

    @Query("UPDATE dubbing_projects SET status = :status, progressPercent = :progress, statusMessage = :message, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateProjectProgress(id: Long, status: ProjectStatus, progress: Int, message: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE dubbing_projects SET status = 'ERROR', errorMessage = :error, updatedAt = :timestamp WHERE id = :id")
    suspend fun setProjectError(id: Long, error: String, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteProject(project: DubbingProject)

    @Query("DELETE FROM dubbing_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    // --- Subtitle Segments Queries ---
    @Query("SELECT * FROM subtitle_segments WHERE projectId = :projectId ORDER BY orderIndex ASC")
    fun getSubtitlesForProject(projectId: Long): Flow<List<SubtitleSegment>>

    @Query("SELECT * FROM subtitle_segments WHERE projectId = :projectId ORDER BY orderIndex ASC")
    suspend fun getSubtitlesForProjectOnce(projectId: Long): List<SubtitleSegment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtitles(subtitles: List<SubtitleSegment>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtitle(subtitle: SubtitleSegment): Long

    @Update
    suspend fun updateSubtitle(subtitle: SubtitleSegment)

    @Query("UPDATE subtitle_segments SET translatedText = :newText, isEditedByUser = 1 WHERE id = :subtitleId")
    suspend fun updateSubtitleText(subtitleId: Long, newText: String)

    @Query("DELETE FROM subtitle_segments WHERE id = :subtitleId")
    suspend fun deleteSubtitle(subtitleId: Long)

    @Query("DELETE FROM subtitle_segments WHERE projectId = :projectId")
    suspend fun clearSubtitlesForProject(projectId: Long)
}
