package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DubbingSettings
import com.example.data.model.LipSyncProvider
import com.example.data.model.TtsProvider
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    currentSettings: DubbingSettings,
    onNavigateBack: () -> Unit,
    onSaveSettings: (DubbingSettings) -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    var settings by remember { mutableStateOf(currentSettings) }
    var geminiKey by remember { mutableStateOf(currentSettings.geminiApiKey) }
    var syncLabsKey by remember { mutableStateOf(currentSettings.syncLabsApiKey) }
    var elevenLabsKey by remember { mutableStateOf(currentSettings.elevenLabsApiKey) }
    var miniMaxKey by remember { mutableStateOf(currentSettings.miniMaxApiKey) }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "تنظیمات و کلیدهای هوش مصنوعی",
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
            // Mode Banner: Offline vs Cloud
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, if (settings.isOfflineMode) AmberGold.copy(alpha = 0.5f) else CyanNeon.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (settings.isOfflineMode) AmberGold else CyanNeon,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "حالت کاملاً آفلاین (حریم خصوصی)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                            )
                            Text(
                                text = "اجرای STT و زیرنویس بدون نیاز به اینترنت با Whisper.cpp محلی",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    Switch(
                        checked = settings.isOfflineMode,
                        onCheckedChange = { settings = settings.copy(isOfflineMode = it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmberGold,
                            checkedTrackColor = AmberGold.copy(alpha = 0.3f),
                            uncheckedThumbColor = CyanNeon,
                            uncheckedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("offline_mode_switch")
                    )
                }
            }

            // Section 1: API Keys Vault
            Text(
                text = "صندوق کلیدهای امن API (اختیاری برای امکانات ابری)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                        Text(
                            text = "کلیدها به صورت رمزنگاری‌شده در دستگاه ذخیره می‌شوند.",
                            style = MaterialTheme.typography.bodySmall.copy(color = EmeraldSuccess)
                        )
                    }

                    // Gemini API Key
                    ApiKeyField(
                        label = "کلید گوگل جمینای (Gemini API Key)",
                        value = geminiKey,
                        placeholder = "کلید جمینای برای ترجمه دیالوگ‌ها به فارسی روان",
                        onValueChange = { geminiKey = it }
                    )

                    // Sync Labs API Key
                    ApiKeyField(
                        label = "کلید Sync Labs API (همگام‌سازی لب)",
                        value = syncLabsKey,
                        placeholder = "برای ساخت ویدیوی Lip-Sync با دقت 4K",
                        onValueChange = { syncLabsKey = it }
                    )

                    // ElevenLabs API Key
                    ApiKeyField(
                        label = "کلید ElevenLabs Dubbing v2",
                        value = elevenLabsKey,
                        placeholder = "تبدیل صوت به صوت با حفظ لحن و احساسات گوینده",
                        onValueChange = { elevenLabsKey = it }
                    )

                    // MiniMax API Key
                    ApiKeyField(
                        label = "کلید MiniMax TTS API (فارسی رسمی)",
                        value = miniMaxKey,
                        placeholder = "تولید گفتار با مدل speech-2.8-hd",
                        onValueChange = { miniMaxKey = it }
                    )
                }
            }

            // Section 2: Default TTS Provider
            Text(
                text = "موتور تولید گفتار و دوبله فارسی (TTS)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TtsProvider.values().forEach { provider ->
                    val isSelected = settings.activeTtsProvider == provider
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PurpleNeon.copy(alpha = 0.15f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) PurpleNeon else DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settings = settings.copy(activeTtsProvider = provider) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = if (isSelected) PurpleNeon else TextSecondary)
                            Column {
                                Text(text = provider.titleFa, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text(text = provider.descriptionFa, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp))
                            }
                        }
                    }
                }
            }

            // Section 3: Default Lip-Sync Provider
            Text(
                text = "موتور همگام‌سازی لب (Lip-Sync)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LipSyncProvider.values().forEach { provider ->
                    val isSelected = settings.activeLipSyncProvider == provider
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AmberGold.copy(alpha = 0.15f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) AmberGold else DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { settings = settings.copy(activeLipSyncProvider = provider) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = if (isSelected) AmberGold else TextSecondary)
                            Column {
                                Text(text = provider.titleFa, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text(text = provider.descriptionFa, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp))
                            }
                        }
                    }
                }
            }

            // Section 4: Audio Ducking Level
            Text(
                text = "کاهش صدای پس‌زمینه اصلی (Audio Ducking): ${settings.audioDuckingPercent}%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
            )

            Slider(
                value = settings.audioDuckingPercent.toFloat(),
                onValueChange = { settings = settings.copy(audioDuckingPercent = it.toInt()) },
                valueRange = 50f..100f,
                steps = 10,
                colors = SliderDefaults.colors(thumbColor = CyanNeon, activeTrackColor = CyanNeon)
            )

            // Cache Cleaning Button
            OutlinedButton(
                onClick = {
                    try {
                        context.cacheDir.listFiles()?.forEach { it.delete() }
                        Toast.makeText(context, "حافظه موقت استودیو با موفقیت پاکسازی شد", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {}
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("پاکسازی کش فایل‌های موقت صوتی و ویدیویی")
            }

            // Save Settings Button
            Button(
                onClick = {
                    val updated = settings.copy(
                        geminiApiKey = geminiKey.trim(),
                        syncLabsApiKey = syncLabsKey.trim(),
                        elevenLabsApiKey = elevenLabsKey.trim(),
                        miniMaxApiKey = miniMaxKey.trim()
                    )
                    onSaveSettings(updated)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_settings_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBackground)
            ) {
                Icon(Icons.Default.Save, contentDescription = null, tint = DarkBackground)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ذخیره تمام تنظیمات", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder, fontSize = 12.sp) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanNeon,
            unfocusedBorderColor = DarkBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = DarkBackground,
            unfocusedContainerColor = DarkBackground
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true
    )
}
