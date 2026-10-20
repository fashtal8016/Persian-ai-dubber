package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SettingsManager
import com.example.data.model.DubbingProject
import com.example.data.model.DubbingSettings
import com.example.data.model.DubbingTone
import com.example.data.model.ProjectStatus
import com.example.data.model.SubtitleSegment
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.VoiceActor
import com.example.data.repository.DubbingRepository
import com.example.domain.service.DeviceVideoItem
import com.example.domain.service.DeviceVideoScanner
import com.example.domain.service.VideoQuality
import com.example.domain.service.WebVideoDownloaderService
import com.example.domain.service.WebVideoInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    DASHBOARD,
    WIZARD_NEW_DUBBING,
    PLAYER,
    SUBTITLE_EDITOR,
    SUBTITLE_STYLING,
    SETTINGS
}

enum class ProjectFilter(val titleFa: String) {
    ALL("همه پروژه‌ها"),
    COMPLETED("آماده پخش"),
    PROCESSING("در حال پردازش")
}

class DubbingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val settingsManager = SettingsManager(application)
    val repository = DubbingRepository(application, database.dubbingDao(), settingsManager)

    private val deviceVideoScanner = DeviceVideoScanner(application)
    val webVideoDownloaderService = WebVideoDownloaderService(application)

    // Navigation and screen state
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _activeFilter = MutableStateFlow(ProjectFilter.ALL)
    val activeFilter: StateFlow<ProjectFilter> = _activeFilter.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<Long?>(null)
    val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

    val projects: StateFlow<List<DubbingProject>> = repository.allProjects
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeProjectSubtitles = MutableStateFlow<List<SubtitleSegment>>(emptyList())
    val activeProjectSubtitles: StateFlow<List<SubtitleSegment>> = _activeProjectSubtitles.asStateFlow()

    private val _activeProject = MutableStateFlow<DubbingProject?>(null)
    val activeProject: StateFlow<DubbingProject?> = _activeProject.asStateFlow()

    val settings: StateFlow<DubbingSettings> = settingsManager.settingsFlow
    val subtitleStyle: StateFlow<SubtitleStyleConfig> = settingsManager.subtitleStyleFlow

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Multi-file device scanner state
    private val _deviceVideos = MutableStateFlow<List<DeviceVideoItem>>(emptyList())
    val deviceVideos: StateFlow<List<DeviceVideoItem>> = _deviceVideos.asStateFlow()

    private val _isScanningVideos = MutableStateFlow(false)
    val isScanningVideos: StateFlow<Boolean> = _isScanningVideos.asStateFlow()

    // Web / YouTube video state
    private val _inspectedWebVideo = MutableStateFlow<WebVideoInfo?>(null)
    val inspectedWebVideo: StateFlow<WebVideoInfo?> = _inspectedWebVideo.asStateFlow()

    private val _isInspectingUrl = MutableStateFlow(false)
    val isInspectingUrl: StateFlow<Boolean> = _isInspectingUrl.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0)
    val downloadProgress: StateFlow<Int> = _downloadProgress.asStateFlow()

    private val _isDownloadingWebVideo = MutableStateFlow(false)
    val isDownloadingWebVideo: StateFlow<Boolean> = _isDownloadingWebVideo.asStateFlow()

    init {
        viewModelScope.launch {
            repository.seedInitialDemoProjectsIfEmpty()
            scanDeviceVideos()
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setFilter(filter: ProjectFilter) {
        _activeFilter.value = filter
    }

    fun scanDeviceVideos() {
        viewModelScope.launch {
            _isScanningVideos.value = true
            try {
                _deviceVideos.value = deviceVideoScanner.scanAllDeviceVideos()
            } catch (e: Exception) {
                // Keep existing or demo videos
            } finally {
                _isScanningVideos.value = false
            }
        }
    }

    fun inspectWebUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _isInspectingUrl.value = true
            try {
                val info = webVideoDownloaderService.inspectUrl(url)
                _inspectedWebVideo.value = info
                _userMessage.value = "ویدیو شناسایی شد: ${info.title.take(30)}..."
            } catch (e: Exception) {
                _userMessage.value = "خطا در بررسی آدرس: ${e.message}"
            } finally {
                _isInspectingUrl.value = false
            }
        }
    }

    fun clearInspectedWebVideo() {
        _inspectedWebVideo.value = null
    }

    fun selectProject(projectId: Long, openScreen: Screen = Screen.PLAYER) {
        _selectedProjectId.value = projectId
        viewModelScope.launch {
            _activeProject.value = repository.getProjectOnce(projectId)
            _activeProjectSubtitles.value = repository.getSubtitlesOnce(projectId)
            _currentScreen.value = openScreen
        }
    }

    fun startNewDubbing(
        title: String,
        videoUri: String,
        tone: DubbingTone,
        voiceActorId: String,
        enableLipSync: Boolean
    ) {
        viewModelScope.launch {
            val projectId = repository.createProject(
                title = if (title.isBlank()) "پروژه دوبله جدید" else title,
                videoUri = videoUri,
                tone = tone,
                voiceActorId = voiceActorId,
                enableLipSync = enableLipSync
            )
            repository.startDubbingPipeline(projectId, viewModelScope)
            _selectedProjectId.value = projectId
            _currentScreen.value = Screen.DASHBOARD
            _userMessage.value = "پردازش ویدیو و تولید دوبله آغاز شد."
        }
    }

    fun startDubbingFromWeb(
        webVideoInfo: WebVideoInfo,
        quality: VideoQuality,
        tone: DubbingTone,
        voiceActorId: String,
        enableLipSync: Boolean
    ) {
        viewModelScope.launch {
            _isDownloadingWebVideo.value = true
            _downloadProgress.value = 10
            _userMessage.value = "در حال دانلود ویدیو با کیفیت ${quality.resolution}..."

            try {
                val downloadedUri = webVideoDownloaderService.downloadVideoWithQuality(
                    videoInfo = webVideoInfo,
                    quality = quality
                ) { percent, _ ->
                    _downloadProgress.value = percent
                }

                _isDownloadingWebVideo.value = false
                _inspectedWebVideo.value = null

                startNewDubbing(
                    title = webVideoInfo.title,
                    videoUri = downloadedUri,
                    tone = tone,
                    voiceActorId = voiceActorId,
                    enableLipSync = enableLipSync
                )
            } catch (e: Exception) {
                _isDownloadingWebVideo.value = false
                _userMessage.value = "خطا در دانلود ویدیو: ${e.message}"
            }
        }
    }

    fun reRunDubbing(projectId: Long) {
        viewModelScope.launch {
            repository.startDubbingPipeline(projectId, viewModelScope)
            _userMessage.value = "پردازش مجدد پروژه آغاز شد."
        }
    }

    fun updateSubtitleText(subtitleId: Long, newText: String) {
        viewModelScope.launch {
            repository.updateSubtitleText(subtitleId, newText)
            _selectedProjectId.value?.let { pId ->
                _activeProjectSubtitles.value = repository.getSubtitlesOnce(pId)
            }
            _userMessage.value = "متن زیرنویس به‌روزرسانی شد."
        }
    }

    fun deleteSubtitle(subtitleId: Long) {
        viewModelScope.launch {
            repository.deleteSubtitle(subtitleId)
            _selectedProjectId.value?.let { pId ->
                _activeProjectSubtitles.value = repository.getSubtitlesOnce(pId)
            }
        }
    }

    fun previewVoiceForSentence(text: String) {
        viewModelScope.launch {
            repository.previewSentenceVoice(text)
        }
    }

    fun updateSubtitleStyle(newStyle: SubtitleStyleConfig) {
        settingsManager.saveSubtitleStyle(newStyle)
    }

    fun updateSettings(newSettings: DubbingSettings) {
        settingsManager.saveSettings(newSettings)
        _userMessage.value = "تنظیمات با موفقیت ذخیره شد."
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_selectedProjectId.value == projectId) {
                _selectedProjectId.value = null
                _activeProject.value = null
                _activeProjectSubtitles.value = emptyList()
                _currentScreen.value = Screen.DASHBOARD
            }
            _userMessage.value = "پروژه حذف شد."
        }
    }

    fun exportSrt(projectId: Long, onExported: (File) -> Unit) {
        viewModelScope.launch {
            val subs = repository.getSubtitlesOnce(projectId)
            val srt = repository.mediaProcessingService.generateSrtContent(subs)
            val file = repository.mediaProcessingService.saveSrtFile(projectId, srt)
            _userMessage.value = "فایل زیرنویس با فرمت SRT ذخیره شد: ${file.name}"
            onExported(file)
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }
}
