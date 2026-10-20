package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignCenter
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubtitleStyleConfig
import com.example.ui.components.PersianSubtitleOverlay
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SubtitleStylingScreen(
    currentStyle: SubtitleStyleConfig,
    onNavigateBack: () -> Unit,
    onSaveStyle: (SubtitleStyleConfig) -> Unit
) {
    BackHandler { onNavigateBack() }

    var config by remember { mutableStateOf(currentStyle) }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "شخصی‌سازی ظاهر زیرنویس فارسی",
                canNavigateBack = true,
                onNavigateBack = onNavigateBack,
                onOpenSettings = null
            )
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Live Preview Card
            Text(
                text = "پیش‌نمایش زنده زیرنویس روی ویدیو",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364))
                            )
                        )
                ) {
                    PersianSubtitleOverlay(
                        subtitleText = "این یک پیش‌نمایش از متن زیرنویس فارسی با قلم وزیرمتن است.",
                        styleConfig = config
                    )
                }
            }

            // Quick Presets
            Text(
                text = "قالب‌های آماده (Presets)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetButton(
                    title = "زرد سینمایی",
                    color = Color(0xFFFFEA00),
                    modifier = Modifier.weight(1f)
                ) {
                    config = SubtitleStyleConfig.YellowCinemaPreset
                }
                PresetButton(
                    title = "کپسول مشکی",
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                ) {
                    config = SubtitleStyleConfig.ModernPillPreset
                }
                PresetButton(
                    title = "نئون سایبری",
                    color = CyanNeon,
                    modifier = Modifier.weight(1f)
                ) {
                    config = SubtitleStyleConfig.NeonCyanPreset
                }
            }

            // Font Selector
            Text(
                text = "انتخاب فونت فارسی",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Vazirmatn" to "وزیرمتن (پیش‌فرض)", "SansSerif" to "سنز سریف", "Default" to "سیستم").forEach { (id, label) ->
                    val isSelected = config.fontChoice == id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) CyanNeon else DarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { config = config.copy(fontChoice = id) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) CyanNeon else TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Font Size Slider
            Text(
                text = "اندازه قلم: ${config.fontSizeSp.toInt()} sp",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Slider(
                value = config.fontSizeSp,
                onValueChange = { config = config.copy(fontSizeSp = it) },
                valueRange = 14f..32f,
                steps = 18,
                colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon),
                modifier = Modifier.testTag("subtitle_font_size_slider")
            )

            // Text Color Palette
            Text(
                text = "رنگ متن",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val colorOptions = listOf(
                    0xFFFFFFFFL to "سفید",
                    0xFFFFEA00L to "زرد",
                    0xFF00E5FFL to "فیروزه‌ای",
                    0xFF00FF66L to "سبز",
                    0xFFFFB300L to "طلایی"
                )
                colorOptions.forEach { (colorVal, _) ->
                    val isSelected = config.textColor == colorVal
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(colorVal))
                            .border(2.dp, if (isSelected) CyanNeon else Color.Transparent, CircleShape)
                            .clickable { config = config.copy(textColor = colorVal) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = if (colorVal == 0xFFFFFFFFL || colorVal == 0xFFFFEA00L) Color.Black else Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            // Stroke / Outline Width Slider
            Text(
                text = "ضخامت حاشیه مشکی (Stroke): ${config.strokeWidth} dp",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Slider(
                value = config.strokeWidth,
                onValueChange = { config = config.copy(strokeWidth = it) },
                valueRange = 0f..6f,
                steps = 12,
                colors = SliderDefaults.colors(thumbColor = PurpleNeon, activeTrackColor = PurpleNeon),
                modifier = Modifier.testTag("subtitle_stroke_slider")
            )

            // Background Opacity
            Text(
                text = "کپسول پشت متن",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0x00000000L to "بدون پس‌زمینه",
                    0x88000000L to "نیمه‌شفاف",
                    0xDD0B111EL to "مشکی مات"
                ).forEach { (bgVal, label) ->
                    val isSelected = config.backgroundColor == bgVal
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) CyanNeon else DarkBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { config = config.copy(backgroundColor = bgVal) }
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSelected) CyanNeon else TextSecondary,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Vertical Position Preset
            Text(
                text = "موقعیت عمودی روی ویدیو",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PositionPresetButton("بالا", 0.15f, Icons.Default.VerticalAlignTop, config.verticalOffsetFraction, Modifier.weight(1f)) {
                    config = config.copy(verticalOffsetFraction = it)
                }
                PositionPresetButton("وسط", 0.50f, Icons.Default.VerticalAlignCenter, config.verticalOffsetFraction, Modifier.weight(1f)) {
                    config = config.copy(verticalOffsetFraction = it)
                }
                PositionPresetButton("پایین", 0.85f, Icons.Default.VerticalAlignBottom, config.verticalOffsetFraction, Modifier.weight(1f)) {
                    config = config.copy(verticalOffsetFraction = it)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Save Style Button
            Button(
                onClick = {
                    onSaveStyle(config)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_subtitle_style_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBackground)
            ) {
                Text(text = "ذخیره و اعمال تنظیمات زیرنویس", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PresetButton(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        }
    }
}

@Composable
private fun PositionPresetButton(
    title: String,
    fraction: Float,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    currentFraction: Float,
    modifier: Modifier = Modifier,
    onClick: (Float) -> Unit
) {
    val isSelected = kotlin.math.abs(currentFraction - fraction) < 0.05f
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PurpleNeon.copy(alpha = 0.2f) else DarkSurfaceCard,
        border = BorderStroke(1.dp, if (isSelected) PurpleNeon else DarkBorder),
        modifier = modifier.clickable { onClick(fraction) }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) PurpleNeon else TextSecondary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (isSelected) PurpleNeon else TextSecondary)
        }
    }
}
