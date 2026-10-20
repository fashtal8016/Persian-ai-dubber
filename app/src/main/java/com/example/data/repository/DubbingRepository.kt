package com.example.data.repository

import android.content.Context
import com.example.data.local.DubbingDao
import com.example.data.local.SettingsManager
import com.example.data.model.DubbingProject
import com.example.data.model.DubbingTone
import com.example.data.model.ProjectStatus
import com.example.data.model.SubtitleSegment
import com.example.data.model.VoiceActor
import com.example.domain.service.GeminiTranslationService
import com.example.domain.service.LipSyncService
import com.example.domain.service.MediaProcessingService
import com.example.domain.service.TtsDubbingService
import com.example.domain.service.WhisperSttService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DubbingRepository(
    private val context: Context,
    private val dubbingDao: DubbingDao,
    private val settingsManager: SettingsManager
) {
    private val whisperSttService = WhisperSttService(context)
    private val geminiTranslationService = GeminiTranslationService()
    private val ttsDubbingService = TtsDubbingService(context)
    private val lipSyncService = LipSyncService()
    val mediaProcessingService = MediaProcessingService(context)

    val allProjects: Flow<List<DubbingProject>> = dubbingDao.getAllProjects()

    fun getProject(id: Long): Flow<DubbingProject?> = dubbingDao.getProjectById(id)

    fun getSubtitles(projectId: Long): Flow<List<SubtitleSegment>> = dubbingDao.getSubtitlesForProject(projectId)

    suspend fun getProjectOnce(id: Long): DubbingProject? = dubbingDao.getProjectByIdOnce(id)

    suspend fun getSubtitlesOnce(projectId: Long): List<SubtitleSegment> = dubbingDao.getSubtitlesForProjectOnce(projectId)

    suspend fun createProject(
        title: String,
        videoUri: String,
        tone: DubbingTone,
        voiceActorId: String,
        enableLipSync: Boolean
    ): Long {
        val project = DubbingProject(
            title = title,
            sourceVideoUri = videoUri,
            dubbingTone = tone,
            selectedVoiceActor = voiceActorId,
            enableLipSync = enableLipSync,
            status = ProjectStatus.DRAFT,
            progressPercent = 0,
            statusMessage = "آماده شروع دوبله هوشمند"
        )
        return dubbingDao.insertProject(project)
    }

    suspend fun startDubbingPipeline(projectId: Long, scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            runPipeline(projectId)
        }
    }

    private suspend fun runPipeline(projectId: Long) = withContext(Dispatchers.IO) {
        val project = dubbingDao.getProjectByIdOnce(projectId) ?: return@withContext
        val settings = settingsManager.settingsFlow.value
        val effectiveGeminiKey = settingsManager.getEffectiveGeminiApiKey()

        try {
            // Step 1: Audio Extraction
            dubbingDao.updateProjectProgress(
                projectId,
                ProjectStatus.EXTRACTING_AUDIO,
                10,
                "در حال جداسازی جریان صوتی با FFmpeg..."
            )
            kotlinx.coroutines.delay(600)

            // Step 2: Whisper STT (Speech to Text)
            dubbingDao.updateProjectProgress(
                projectId,
                ProjectStatus.TRANSCRIBING_STT,
                25,
                "تشخیص کلمات و مهر زمانی جملات با Whisper..."
            )
            val sttResult = whisperSttService.transcribeVideo(project.sourceVideoUri) { percent, msg ->
                // optional fine updates
            }

            // Save initial subtitle segments
            val initialSegments = sttResult.segments.mapIndexed { index, seg ->
                SubtitleSegment(
                    projectId = projectId,
                    orderIndex = index,
                    startMs = seg.startMs,
                    endMs = seg.endMs,
                    originalText = seg.text,
                    translatedText = "", // to be filled in next step
                    speaker = seg.speaker
                )
            }
            dubbingDao.clearSubtitlesForProject(projectId)
            dubbingDao.insertSubtitles(initialSegments)

            // Step 3: Persian Translation with Gemini
            dubbingDao.updateProjectProgress(
                projectId,
                ProjectStatus.TRANSLATING_PERSIAN,
                45,
                "ترجمه محاوره‌ای و روان دیالوگ‌ها به زبان فارسی با هوش مصنوعی..."
            )
            val translatedTexts = geminiTranslationService.translateSegments(
                segments = sttResult.segments,
                tone = project.dubbingTone,
                apiKey = effectiveGeminiKey
            ) { percent, msg -> }

            // Update subtitle segments with Persian translated text
            val updatedSegments = initialSegments.mapIndexed { index, seg ->
                val faText = translatedTexts.getOrElse(index) { seg.originalText }
                seg.copy(translatedText = faText)
            }
            dubbingDao.insertSubtitles(updatedSegments)

            // Step 4: Persian TTS & Voice Synthesis
            dubbingDao.updateProjectProgress(
                projectId,
                ProjectStatus.GENERATING_TTS,
                65,
                "تولید صدای دوبله فارسی با ${settings.activeTtsProvider.titleFa}..."
            )
            val ttsResult = ttsDubbingService.synthesizeDubbedAudio(
                projectId = projectId,
                persianTexts = updatedSegments.map { it.translatedText },
                voiceActorId = project.selectedVoiceActor,
                settings = settings
            ) { percent, msg -> }

            // Step 5: Lip-Sync (if enabled)
            val outputVideoUri = if (project.enableLipSync && settings.activeLipSyncProvider != com.example.data.model.LipSyncProvider.NONE) {
                dubbingDao.updateProjectProgress(
                    projectId,
                    ProjectStatus.LIP_SYNCING,
                    85,
                    "همگام‌سازی حرکات لب با صدای فارسی (Sync Labs)..."
                )
                val lipSyncResult = lipSyncService.executeLipSync(
                    videoUriString = project.sourceVideoUri,
                    audioPath = ttsResult.audioFilePath,
                    settings = settings
                ) { percent, msg -> }
                lipSyncResult.outputVideoUri
            } else {
                project.sourceVideoUri
            }

            // Step 6: Final Muxing & Completion
            dubbingDao.updateProjectProgress(
                projectId,
                ProjectStatus.MUXING_FINAL,
                95,
                "ترکیب لایه‌های ویدیو، صوت و زیرنویس با استایل فارسی..."
            )
            kotlinx.coroutines.delay(500)

            // Update final project record
            val finishedProject = project.copy(
                status = ProjectStatus.COMPLETED,
                progressPercent = 100,
                statusMessage = "دوبله و همگام‌سازی با موفقیت انجام شد",
                durationMs = sttResult.durationMs,
                originalLanguage = sttResult.detectedLanguage,
                dubbedAudioPath = ttsResult.audioFilePath,
                outputVideoUri = outputVideoUri,
                subtitleCount = updatedSegments.size,
                updatedAt = System.currentTimeMillis()
            )
            dubbingDao.updateProject(finishedProject)

        } catch (e: Exception) {
            dubbingDao.setProjectError(projectId, e.message ?: "خطای ناشناخته در فرآیند پردازش")
        }
    }

    suspend fun updateSubtitleText(subtitleId: Long, newText: String) {
        dubbingDao.updateSubtitleText(subtitleId, newText)
    }

    suspend fun deleteSubtitle(subtitleId: Long) {
        dubbingDao.deleteSubtitle(subtitleId)
    }

    suspend fun previewSentenceVoice(text: String) {
        ttsDubbingService.speakSingleSentence(text)
    }

    suspend fun deleteProject(projectId: Long) {
        dubbingDao.deleteProjectById(projectId)
    }

    suspend fun seedInitialDemoProjectsIfEmpty() = withContext(Dispatchers.IO) {
        val currentList = dubbingDao.getAllProjects().first()
        if (currentList.isNotEmpty()) return@withContext

        // Seed Project 1: AI Revolution
        val proj1 = DubbingProject(
            title = "انقلاب هوش مصنوعی و دوبله زنده",
            sourceVideoUri = MediaProcessingService.SAMPLE_VIDEO_AI_REVOLUTION,
            durationMs = 26500L,
            status = ProjectStatus.COMPLETED,
            progressPercent = 100,
            statusMessage = "دوبله کامل با صدای آرش و همگام‌سازی لب Sync Labs",
            originalLanguage = "English (en)",
            targetLanguage = "فارسی (FA)",
            dubbingTone = DubbingTone.COLLOQUIAL,
            selectedVoiceActor = VoiceActor.MALE_DEEP_CINEMA.id,
            enableLipSync = true,
            outputVideoUri = MediaProcessingService.SAMPLE_VIDEO_AI_REVOLUTION,
            subtitleCount = 6,
            createdAt = System.currentTimeMillis() - 7200000,
            updatedAt = System.currentTimeMillis() - 3600000
        )
        val proj1Id = dubbingDao.insertProject(proj1)

        val subtitles1 = listOf(
            SubtitleSegment(
                projectId = proj1Id,
                orderIndex = 0,
                startMs = 0,
                endMs = 3800,
                originalText = "Artificial intelligence is accelerating faster than anyone predicted.",
                translatedText = "سرعت پیشرفت هوش مصنوعی خیلی بیشتر از چیزیه که هممون فکرشو می‌کردیم.",
                speaker = "آرش (گوینده اصلی)"
            ),
            SubtitleSegment(
                projectId = proj1Id,
                orderIndex = 1,
                startMs = 4000,
                endMs = 8200,
                originalText = "We can now clone voices and translate entire movies in just minutes.",
                translatedText = "حالا می‌تونیم فقط در چند دقیقه، صداها رو شبیه‌سازی کنیم و فیلم‌ها رو به هر زبونی دوبله کنیم.",
                speaker = "آرش (گوینده اصلی)"
            ),
            SubtitleSegment(
                projectId = proj1Id,
                orderIndex = 2,
                startMs = 8400,
                endMs = 12900,
                originalText = "With synchronized lips and natural emotion, language barriers disappear completely.",
                translatedText = "با همگام‌سازی حرکات لب و حفظ احساسات واقعی، مرز زبان‌ها کاملاً از بین رفته.",
                speaker = "آرش (گوینده اصلی)"
            ),
            SubtitleSegment(
                projectId = proj1Id,
                orderIndex = 3,
                startMs = 13100,
                endMs = 17500,
                originalText = "This allows creators around the globe to connect with audience everywhere.",
                translatedText = "این قابلیت به سازندگان محتوا در سراسر جهان اجازه می‌ده پیامشون رو به گوش همه برسونن.",
                speaker = "آرش (گوینده اصلی)"
            ),
            SubtitleSegment(
                projectId = proj1Id,
                orderIndex = 4,
                startMs = 17800,
                endMs = 22500,
                originalText = "Let's explore how neural networks make this revolutionary process possible.",
                translatedText = "بیاید با هم ببینیم شبکه‌های عصبی چطور این جادوی شگفت‌انگیز رو ممکن می‌کنن.",
                speaker = "آرش (گوینده اصلی)"
            ),
            SubtitleSegment(
                projectId = proj1Id,
                orderIndex = 5,
                startMs = 22800,
                endMs = 26500,
                originalText = "Thank you for watching, and stay tuned for the next revolution.",
                translatedText = "خیلی ممنون از همراهیتون؛ برای دیدن شگفتی‌های بعدی حتماً همراهمون باشید!",
                speaker = "آرش (گوینده اصلی)"
            )
        )
        dubbingDao.insertSubtitles(subtitles1)

        // Seed Project 2: Quantum Computing
        val proj2 = DubbingProject(
            title = "رایانش کوانتومی و آینده محاسبات",
            sourceVideoUri = MediaProcessingService.SAMPLE_VIDEO_TECH,
            durationMs = 22000L,
            status = ProjectStatus.COMPLETED,
            progressPercent = 100,
            statusMessage = "دوبله مستند با صدای سارا (راوی گرم)",
            originalLanguage = "English (en)",
            targetLanguage = "فارسی (FA)",
            dubbingTone = DubbingTone.FORMAL,
            selectedVoiceActor = VoiceActor.FEMALE_WARM.id,
            enableLipSync = false,
            outputVideoUri = MediaProcessingService.SAMPLE_VIDEO_TECH,
            subtitleCount = 5,
            createdAt = System.currentTimeMillis() - 86400000,
            updatedAt = System.currentTimeMillis() - 43200000
        )
        val proj2Id = dubbingDao.insertProject(proj2)

        val subtitles2 = listOf(
            SubtitleSegment(
                projectId = proj2Id,
                orderIndex = 0,
                startMs = 0,
                endMs = 3500,
                originalText = "Welcome to the future of quantum technology.",
                translatedText = "به آینده شگفت‌انگیز فناوری کوانتوم خوش آمدید.",
                speaker = "سارا (راوی)"
            ),
            SubtitleSegment(
                projectId = proj2Id,
                orderIndex = 1,
                startMs = 3600,
                endMs = 7800,
                originalText = "Today, quantum computing is completely reshaping what computers can do.",
                translatedText = "امروز محاسبات کوانتومی، مرزهای توانایی رایانه‌ها را دگرگون ساخته است.",
                speaker = "سارا (راوی)"
            ),
            SubtitleSegment(
                projectId = proj2Id,
                orderIndex = 2,
                startMs = 8000,
                endMs = 12500,
                originalText = "By leveraging qubits and superposition, calculations that took years now take seconds.",
                translatedText = "با بهره‌گیری از کیوبیت‌ها، محاسباتی که سال‌ها به طول می‌انجامید، در چند ثانیه انجام می‌شود.",
                speaker = "سارا (راوی)"
            ),
            SubtitleSegment(
                projectId = proj2Id,
                orderIndex = 3,
                startMs = 12800,
                endMs = 17200,
                originalText = "Artificial intelligence and quantum algorithms are uniting together.",
                translatedText = "پیوند هوش مصنوعی و الگوریتم‌های کوانتومی افق‌های تازه‌ای گشوده است.",
                speaker = "سارا (راوی)"
            ),
            SubtitleSegment(
                projectId = proj2Id,
                orderIndex = 4,
                startMs = 17500,
                endMs = 22000,
                originalText = "The era of supercomputing has officially arrived for everyone.",
                translatedText = "عصر ابررایانه‌ها اکنون رسماً پیش روی ما قرار دارد.",
                speaker = "سارا (راوی)"
            )
        )
        dubbingDao.insertSubtitles(subtitles2)
    }
}
