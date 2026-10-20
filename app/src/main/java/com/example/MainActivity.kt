package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.DubbingViewModel
import com.example.ui.Screen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DubbingWizardScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubtitleEditorScreen
import com.example.ui.screens.SubtitleStylingScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: DubbingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme(darkTheme = true) {
                // Persian UI layout direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = DarkBackground
                    ) {
                        AppNavigation(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: DubbingViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val activeSubtitles by viewModel.activeProjectSubtitles.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val subtitleStyle by viewModel.subtitleStyle.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()
    val deviceVideos by viewModel.deviceVideos.collectAsStateWithLifecycle()
    val isScanningVideos by viewModel.isScanningVideos.collectAsStateWithLifecycle()
    val inspectedWebVideo by viewModel.inspectedWebVideo.collectAsStateWithLifecycle()
    val isInspectingUrl by viewModel.isInspectingUrl.collectAsStateWithLifecycle()
    val downloadProgress by viewModel.downloadProgress.collectAsStateWithLifecycle()
    val isDownloadingWebVideo by viewModel.isDownloadingWebVideo.collectAsStateWithLifecycle()

    when (currentScreen) {
        Screen.DASHBOARD -> {
            DashboardScreen(
                projects = projects,
                activeFilter = activeFilter,
                isOfflineMode = settings.isOfflineMode,
                userMessage = userMessage,
                onFilterChanged = { viewModel.setFilter(it) },
                onOpenWizard = { viewModel.navigateTo(Screen.WIZARD_NEW_DUBBING) },
                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                onSelectProjectForPlay = { projectId ->
                    viewModel.selectProject(projectId, Screen.PLAYER)
                },
                onSelectProjectForSubtitles = { projectId ->
                    viewModel.selectProject(projectId, Screen.SUBTITLE_EDITOR)
                },
                onReRunDubbing = { projectId ->
                    viewModel.reRunDubbing(projectId)
                },
                onDeleteProject = { projectId ->
                    viewModel.deleteProject(projectId)
                },
                onClearUserMessage = { viewModel.clearUserMessage() }
            )
        }

        Screen.WIZARD_NEW_DUBBING -> {
            DubbingWizardScreen(
                deviceVideos = deviceVideos,
                isScanningVideos = isScanningVideos,
                inspectedWebVideo = inspectedWebVideo,
                isInspectingUrl = isInspectingUrl,
                downloadProgress = downloadProgress,
                isDownloadingWebVideo = isDownloadingWebVideo,
                onNavigateBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                onScanVideos = { viewModel.scanDeviceVideos() },
                onInspectWebUrl = { viewModel.inspectWebUrl(it) },
                onStartDubbing = { title, uri, tone, actorId, lipSync ->
                    viewModel.startNewDubbing(title, uri, tone, actorId, lipSync)
                },
                onStartDubbingFromWeb = { info, q, tone, actorId, lipSync ->
                    viewModel.startDubbingFromWeb(info, q, tone, actorId, lipSync)
                },
                onPreviewVoice = { text ->
                    viewModel.previewVoiceForSentence(text)
                }
            )
        }

        Screen.PLAYER -> {
            VideoPlayerScreen(
                project = activeProject,
                subtitles = activeSubtitles,
                subtitleStyle = subtitleStyle,
                onNavigateBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                onOpenSubtitleEditor = { viewModel.navigateTo(Screen.SUBTITLE_EDITOR) },
                onOpenSubtitleStyling = { viewModel.navigateTo(Screen.SUBTITLE_STYLING) },
                onUpdateSubtitleStyle = { newStyle ->
                    viewModel.updateSubtitleStyle(newStyle)
                }
            )
        }

        Screen.SUBTITLE_EDITOR -> {
            SubtitleEditorScreen(
                project = activeProject,
                subtitles = activeSubtitles,
                onNavigateBack = {
                    if (activeProject != null) viewModel.navigateTo(Screen.PLAYER)
                    else viewModel.navigateTo(Screen.DASHBOARD)
                },
                onUpdateSubtitleText = { id, text ->
                    viewModel.updateSubtitleText(id, text)
                },
                onDeleteSubtitle = { id ->
                    viewModel.deleteSubtitle(id)
                },
                onPreviewVoice = { text ->
                    viewModel.previewVoiceForSentence(text)
                },
                onExportSrt = { pId ->
                    viewModel.exportSrt(pId) {}
                }
            )
        }

        Screen.SUBTITLE_STYLING -> {
            SubtitleStylingScreen(
                currentStyle = subtitleStyle,
                onNavigateBack = {
                    if (activeProject != null) viewModel.navigateTo(Screen.PLAYER)
                    else viewModel.navigateTo(Screen.DASHBOARD)
                },
                onSaveStyle = { newStyle ->
                    viewModel.updateSubtitleStyle(newStyle)
                }
            )
        }

        Screen.SETTINGS -> {
            SettingsScreen(
                currentSettings = settings,
                onNavigateBack = { viewModel.navigateTo(Screen.DASHBOARD) },
                onSaveSettings = { newSettings ->
                    viewModel.updateSettings(newSettings)
                }
            )
        }
    }
}
