package com.example.data.model

enum class TtsProvider(val titleFa: String, val descriptionFa: String) {
    MINIMAX("MiniMax TTS HD", "پشتیبانی رسمی از فارسی با مدل speech-2.8-hd و لحن طبیعی"),
    ELEVENLABS("ElevenLabs Dubbing v2", "تبدیل صوت به صوت با حفظ دقیق احساسات و لحن گوینده"),
    GEMINI_AUDIO("Gemini AI Audio", "تولید صدای باکیفیت و هماهنگ با هوش مصنوعی گوگل"),
    SYSTEM_TTS("موتور محلی اندروید (آفلاین)", "تولید گفتار با موتور صوتی دستگاه بدون نیاز به اینترنت")
}

enum class LipSyncProvider(val titleFa: String, val descriptionFa: String) {
    SYNC_LABS("Sync Labs (lipsync-2 / sync-3)", "همگام‌سازی حرکات لب با هوش مصنوعی و کیفیت بالا"),
    HEYGEN("HeyGen Precision API", "همگام‌سازی لب دقیق برای ویدیوهای طولانی و چهره باز"),
    NONE("غیرفعال (فقط جایگزینی صدا)", "دوبله صوتی و زیرنویس بدون تغییر حرکات چهره")
}

data class DubbingSettings(
    val activeTtsProvider: TtsProvider = TtsProvider.MINIMAX,
    val activeLipSyncProvider: LipSyncProvider = LipSyncProvider.SYNC_LABS,
    val geminiApiKey: String = "",
    val syncLabsApiKey: String = "",
    val elevenLabsApiKey: String = "",
    val miniMaxApiKey: String = "",
    val isOfflineMode: Boolean = false,
    val autoLipSync: Boolean = true,
    val audioDuckingPercent: Int = 85, // Volume reduction of original background sound
    val keepOriginalAudioSoftly: Boolean = true
)
