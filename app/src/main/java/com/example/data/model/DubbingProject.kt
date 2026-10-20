package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ProjectStatus(val titleFa: String) {
    DRAFT("پیش‌نویس"),
    EXTRACTING_AUDIO("استخراج صدا"),
    TRANSCRIBING_STT("تشخیص گفتار (Whisper)"),
    TRANSLATING_PERSIAN("ترجمه به فارسی (Gemini)"),
    GENERATING_TTS("تولید صدای فارسی"),
    LIP_SYNCING("همگام‌سازی لب (Lip-Sync)"),
    MUXING_FINAL("ترکیب نهایی ویدیو"),
    COMPLETED("آماده پخش"),
    ERROR("خطا در پردازش")
}

enum class DubbingTone(val titleFa: String, val promptInstruction: String) {
    COLLOQUIAL("محاوره‌ای و طبیعی", "ترجمه را به زبان فارسی محاوره‌ای، روان و صمیمی، مناسب دوبله عامیانه و بدون لغات کتابی خشک انجام بده."),
    FORMAL("رسمی و مستند", "ترجمه را به زبان فارسی استاندارد، شیوا و مناسب مستندهای علمی و رسمی بنویس."),
    CINEMATIC("سینمایی و دراماتیک", "ترجمه را با لحن پرکشش، احساسی و مناسب دیالوگ‌های فیلم‌های سینمایی اکشن یا درام تنظیم کن."),
    COMEDY("طنز و شاداب", "ترجمه را با لحن شاداب، بامزه و با استفاده از اصطلاحات شیرین کوچه بازاری فارسی بنویس.")
}

enum class VoiceActor(val id: String, val titleFa: String, val genderFa: String, val sampleDesc: String) {
    MALE_DEEP_CINEMA("fa_male_cinema", "آرش (دوبلور سینمایی بم)", "مرد", "صدای بم، گیرا و پرطنین مناسب نقش‌های اصلی و درام"),
    MALE_ENERGETIC("fa_male_energetic", "سامان (راوی پرانرژی)", "مرد", "صدای رسا و جوان مناسب یوتیوب و آموزش"),
    FEMALE_WARM("fa_female_warm", "سارا (راوی گرم و شفاف)", "زن", "صدای دلنشین، آرام و شفاف مناسب مستند و داستان"),
    FEMALE_CINEMATIC("fa_female_cinema", "نیلوفر (دوبلور دراماتیک)", "زن", "صدای احساسی، منعطف و جذاب مناسب دیالوگ‌های پرهیجان")
}

@Entity(tableName = "dubbing_projects")
data class DubbingProject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sourceVideoUri: String,
    val durationMs: Long = 0L,
    val status: ProjectStatus = ProjectStatus.DRAFT,
    val progressPercent: Int = 0,
    val statusMessage: String = "",
    val errorMessage: String? = null,
    val originalLanguage: String = "English",
    val targetLanguage: String = "فارسی (FA)",
    val dubbingTone: DubbingTone = DubbingTone.COLLOQUIAL,
    val selectedVoiceActor: String = VoiceActor.MALE_DEEP_CINEMA.id,
    val enableLipSync: Boolean = true,
    val originalAudioPath: String? = null,
    val dubbedAudioPath: String? = null,
    val outputVideoUri: String? = null,
    val subtitleCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
