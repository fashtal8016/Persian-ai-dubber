package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.BuildConfig
import com.example.data.model.DubbingSettings
import com.example.data.model.LipSyncProvider
import com.example.data.model.SubtitleStyleConfig
import com.example.data.model.TtsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("persian_dubber_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: StateFlow<DubbingSettings> = _settingsFlow.asStateFlow()

    private val _subtitleStyleFlow = MutableStateFlow(loadSubtitleStyle())
    val subtitleStyleFlow: StateFlow<SubtitleStyleConfig> = _subtitleStyleFlow.asStateFlow()

    private fun loadSettings(): DubbingSettings {
        val ttsName = prefs.getString("tts_provider", TtsProvider.MINIMAX.name) ?: TtsProvider.MINIMAX.name
        val lipSyncName = prefs.getString("lipsync_provider", LipSyncProvider.SYNC_LABS.name) ?: LipSyncProvider.SYNC_LABS.name
        val geminiKey = prefs.getString("gemini_api_key", "") ?: ""
        val syncLabsKey = prefs.getString("synclabs_api_key", "") ?: ""
        val elevenLabsKey = prefs.getString("elevenlabs_api_key", "") ?: ""
        val miniMaxKey = prefs.getString("minimax_api_key", "") ?: ""
        val isOffline = prefs.getBoolean("is_offline_mode", false)
        val autoLipSync = prefs.getBoolean("auto_lipsync", true)
        val ducking = prefs.getInt("audio_ducking", 85)

        return DubbingSettings(
            activeTtsProvider = try { TtsProvider.valueOf(ttsName) } catch (e: Exception) { TtsProvider.MINIMAX },
            activeLipSyncProvider = try { LipSyncProvider.valueOf(lipSyncName) } catch (e: Exception) { LipSyncProvider.SYNC_LABS },
            geminiApiKey = geminiKey,
            syncLabsApiKey = syncLabsKey,
            elevenLabsApiKey = elevenLabsKey,
            miniMaxApiKey = miniMaxKey,
            isOfflineMode = isOffline,
            autoLipSync = autoLipSync,
            audioDuckingPercent = ducking
        )
    }

    private fun loadSubtitleStyle(): SubtitleStyleConfig {
        return SubtitleStyleConfig(
            isVisible = prefs.getBoolean("sub_visible", true),
            fontChoice = prefs.getString("sub_font", "Vazirmatn") ?: "Vazirmatn",
            fontSizeSp = prefs.getFloat("sub_size", 18f),
            textColor = prefs.getLong("sub_color", 0xFFFFFFFFL),
            strokeColor = prefs.getLong("sub_stroke_color", 0xFF000000L),
            strokeWidth = prefs.getFloat("sub_stroke_width", 2.5f),
            backgroundColor = prefs.getLong("sub_bg_color", 0x88000000L),
            verticalOffsetFraction = prefs.getFloat("sub_v_offset", 0.85f),
            isBold = prefs.getBoolean("sub_is_bold", true)
        )
    }

    fun saveSettings(settings: DubbingSettings) {
        prefs.edit()
            .putString("tts_provider", settings.activeTtsProvider.name)
            .putString("lipsync_provider", settings.activeLipSyncProvider.name)
            .putString("gemini_api_key", settings.geminiApiKey)
            .putString("synclabs_api_key", settings.syncLabsApiKey)
            .putString("elevenlabs_api_key", settings.elevenLabsApiKey)
            .putString("minimax_api_key", settings.miniMaxApiKey)
            .putBoolean("is_offline_mode", settings.isOfflineMode)
            .putBoolean("auto_lipsync", settings.autoLipSync)
            .putInt("audio_ducking", settings.audioDuckingPercent)
            .apply()
        _settingsFlow.value = settings
    }

    fun saveSubtitleStyle(style: SubtitleStyleConfig) {
        prefs.edit()
            .putBoolean("sub_visible", style.isVisible)
            .putString("sub_font", style.fontChoice)
            .putFloat("sub_size", style.fontSizeSp)
            .putLong("sub_color", style.textColor)
            .putLong("sub_stroke_color", style.strokeColor)
            .putFloat("sub_stroke_width", style.strokeWidth)
            .putLong("sub_bg_color", style.backgroundColor)
            .putFloat("sub_v_offset", style.verticalOffsetFraction)
            .putBoolean("sub_is_bold", style.isBold)
            .apply()
        _subtitleStyleFlow.value = style
    }

    fun getEffectiveGeminiApiKey(): String {
        val customKey = _settingsFlow.value.geminiApiKey.trim()
        if (customKey.isNotEmpty()) return customKey
        val buildKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        return if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") buildKey else ""
    }
}
