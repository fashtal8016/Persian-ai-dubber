package com.example.data.model

import androidx.compose.ui.graphics.Color

data class SubtitleStyleConfig(
    val isVisible: Boolean = true,
    val fontChoice: String = "Vazirmatn",
    val fontSizeSp: Float = 18f,
    val textColor: Long = 0xFFFFFFFFL,
    val strokeColor: Long = 0xFF000000L,
    val strokeWidth: Float = 2.5f,
    val backgroundColor: Long = 0x88000000L,
    val backgroundCornerRadiusDp: Float = 8f,
    val verticalOffsetFraction: Float = 0.85f, // 0.1f top, 0.5f center, 0.85f bottom
    val isBold: Boolean = true,
    val isSoftShadowEnabled: Boolean = true
) {
    fun composeTextColor(): Color = Color(textColor)
    fun composeStrokeColor(): Color = Color(strokeColor)
    fun composeBackgroundColor(): Color = Color(backgroundColor)

    companion object {
        val Default = SubtitleStyleConfig()

        val YellowCinemaPreset = SubtitleStyleConfig(
            isVisible = true,
            fontChoice = "Vazirmatn",
            fontSizeSp = 20f,
            textColor = 0xFFFFEA00L,
            strokeColor = 0xFF000000L,
            strokeWidth = 3f,
            backgroundColor = 0x00000000L,
            verticalOffsetFraction = 0.86f,
            isBold = true
        )

        val ModernPillPreset = SubtitleStyleConfig(
            isVisible = true,
            fontChoice = "Vazirmatn",
            fontSizeSp = 17f,
            textColor = 0xFFFFFFFFL,
            strokeColor = 0x00000000L,
            strokeWidth = 0f,
            backgroundColor = 0xCC0B111EL,
            verticalOffsetFraction = 0.84f,
            isBold = true
        )

        val NeonCyanPreset = SubtitleStyleConfig(
            isVisible = true,
            fontChoice = "Vazirmatn",
            fontSizeSp = 19f,
            textColor = 0xFF00E5FFL,
            strokeColor = 0xFF000000L,
            strokeWidth = 2.5f,
            backgroundColor = 0x66000000L,
            verticalOffsetFraction = 0.85f,
            isBold = true
        )
    }
}
