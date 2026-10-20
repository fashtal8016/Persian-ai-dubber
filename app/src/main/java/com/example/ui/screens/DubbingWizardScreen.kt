package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DubbingTone
import com.example.data.model.VoiceActor
import com.example.domain.service.DeviceVideoItem
import com.example.domain.service.MediaProcessingService
import com.example.domain.service.VideoQuality
import com.example.domain.service.WebVideoInfo
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DubbingWizardScreen(
    deviceVideos: List<DeviceVideoItem>,
    isScanningVideos: Boolean,
    inspectedWebVideo: WebVideoInfo?,
    isInspectingUrl: Boolean,
    downloadProgress: Int,
    isDownloadingWebVideo: Boolean,
    onNavigateBack: () -> Unit,
    onScanVideos: () -> Unit,
    onInspectWebUrl: (String) -> Unit,
    onStartDubbing: (title: String, videoUri: String, tone: DubbingTone, voiceActorId: String, enableLipSync: Boolean) -> Unit,
    onStartDubbingFromWeb: (webVideoInfo: WebVideoInfo, quality: VideoQuality, tone: DubbingTone, voiceActorId: String, enableLipSync: Boolean) -> Unit,
    onPreviewVoice: (String) -> Unit
) {
    var selectedSourceTabIndex by remember { mutableIntStateOf(0) } // 0 = Phone Gallery / Scanner, 1 = YouTube & Web URL

    var title by remember { mutableStateOf("دوبله ویدیوی جدید") }
    var selectedVideoUri by remember { mutableStateOf<String?>(MediaProcessingService.SAMPLE_VIDEO_AI_REVOLUTION) }
    var videoDisplayName by remember { mutableStateOf("ویدیوی نمونه: انقلاب هوش مصنوعی") }
    var selectedTone by remember { mutableStateOf(DubbingTone.COLLOQUIAL) }
    var selectedVoiceActor by remember { mutableStateOf(VoiceActor.MALE_DEEP_CINEMA) }
    var enableLipSync by remember { mutableStateOf(true) }

    // Web URL state
    var inputUrl by remember { mutableStateOf("https://www.youtube.com/watch?v=2r1p8iTz2oA") }
    var selectedQuality by remember { mutableStateOf(VideoQuality.FULL_HD_1080P) }

    // Search query for on-device videos
    var searchQuery by remember { mutableStateOf("") }

    // Android Photo/Video Picker (Zero-permission)
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            selectedVideoUri = it.toString()
            videoDisplayName = "ویدیوی انتخابی از حافظه دستگاه"
            if (title == "دوبله ویدیوی جدید") {
                title = "پروژه دوبله گالری"
            }
        }
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "پیکربندی و انتخاب ویدیوی دوبله",
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
            // Source Selection Tabs: Phone Videos vs YouTube/Web
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    // Tab 1: Phone Videos
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedSourceTabIndex == 0) CyanNeon.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (selectedSourceTabIndex == 0) BorderStroke(1.dp, CyanNeon) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSourceTabIndex = 0 }
                            .testTag("tab_phone_videos")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = if (selectedSourceTabIndex == 0) CyanNeon else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ویدیوهای روی گوشی",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedSourceTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedSourceTabIndex == 0) CyanNeon else TextSecondary
                            )
                        }
                    }

                    // Tab 2: YouTube & Web URL
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedSourceTabIndex == 1) PurpleNeon.copy(alpha = 0.2f) else Color.Transparent,
                        border = if (selectedSourceTabIndex == 1) BorderStroke(1.dp, PurpleNeon) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSourceTabIndex = 1 }
                            .testTag("tab_web_youtube")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.OndemandVideo,
                                contentDescription = null,
                                tint = if (selectedSourceTabIndex == 1) PurpleNeon else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "لینک یوتیوب و وب",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (selectedSourceTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (selectedSourceTabIndex == 1) PurpleNeon else TextSecondary
                            )
                        }
                    }
                }
            }

            // TAB 0: On-Device Video Browser / Scanner
            if (selectedSourceTabIndex == 0) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "۱. اسکن و انتخاب تمام ویدیوهای گوشی (${deviceVideos.size} ویدیو یافت شد)",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
                        )

                        IconButton(
                            onClick = onScanVideos,
                            modifier = Modifier.testTag("rescan_videos_button")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "اسکن مجدد", tint = CyanNeon)
                        }
                    }

                    // Search and System Picker Action Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("جستجوی نام ویدیو...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = DarkBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurfaceCard,
                                unfocusedContainerColor = DarkSurfaceCard
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedButton(
                            onClick = {
                                videoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                                )
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = CyanNeon),
                            border = BorderStroke(1.dp, CyanNeon.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_system_picker_btn")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("انتخاب دستی")
                        }
                    }

                    // Video Cards List
                    val filteredVideos = if (searchQuery.isBlank()) {
                        deviceVideos
                    } else {
                        deviceVideos.filter {
                            it.title.contains(searchQuery, ignoreCase = true) ||
                            it.displayName.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (isScanningVideos) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CyanNeon)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredVideos.forEach { video ->
                                val isSelected = selectedVideoUri == video.uriString
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CyanNeon.copy(alpha = 0.15f) else DarkSurfaceCard,
                                    border = BorderStroke(1.dp, if (isSelected) CyanNeon else DarkBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedVideoUri = video.uriString
                                            videoDisplayName = video.title
                                            title = video.title
                                        }
                                        .testTag("device_video_item_${video.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(46.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) CyanNeon else DarkSurfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = if (isSelected) DarkBackground else CyanNeon,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = video.title,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⏱ ${video.formattedDuration()}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                                )
                                                Text(
                                                    text = "💾 ${video.formattedSize()}",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                                )
                                                Text(
                                                    text = video.resolution,
                                                    style = MaterialTheme.typography.bodySmall.copy(color = PurpleNeon, fontSize = 11.sp)
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(24.dp)
                                                    .clip(CircleShape)
                                                    .background(CyanNeon),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: YouTube & Web Video Downloader
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "۱. درج آدرس ویدیوی یوتیوب یا سایت اینترنتی",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PurpleNeon)
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("آدرس ویدیو (YouTube, Vimeo, MP4 Direct Link)") },
                        placeholder = { Text("https://www.youtube.com/watch?v=...") },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, tint = PurpleNeon) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("web_video_url_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PurpleNeon,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceCard,
                            unfocusedContainerColor = DarkSurfaceCard
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick URL Presets
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        QuickUrlChip("یوتیوب: فناوری کوانتوم", "https://www.youtube.com/watch?v=JhHMJCUmq28", Modifier.weight(1f)) {
                            inputUrl = it
                            onInspectWebUrl(it)
                        }
                        QuickUrlChip("مستند حیات وحش", "https://www.youtube.com/watch?v=nature_wild_2026", Modifier.weight(1f)) {
                            inputUrl = it
                            onInspectWebUrl(it)
                        }
                    }

                    // Inspect Button
                    Button(
                        onClick = { onInspectWebUrl(inputUrl) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("inspect_web_url_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleNeon, contentColor = DarkBackground)
                    ) {
                        if (isInspectingUrl) {
                            CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(20.dp))
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("شناسایی مشخصات و استخراج کیفیت‌ها", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Inspected Video Details Card
                    inspectedWebVideo?.let { info ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            border = BorderStroke(1.dp, PurpleNeon.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(PurpleNeon.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = PurpleNeon, modifier = Modifier.size(24.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = info.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary), maxLines = 2)
                                        Text(text = "منبع: ${info.channelOrHost} • زبان مبدأ: ${info.detectedLanguage} • زمان: ${info.formattedDuration()}", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                                    }
                                }

                                // Quality Selection
                                Text(
                                    text = "کیفیت تصویر خروجی جهت دوبله:",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AmberGold)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    VideoQuality.values().forEach { q ->
                                        val isQSelected = selectedQuality == q
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isQSelected) AmberGold.copy(alpha = 0.2f) else DarkSurface,
                                            border = BorderStroke(1.dp, if (isQSelected) AmberGold else DarkBorder),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { selectedQuality = q }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(Icons.Default.HighQuality, contentDescription = null, tint = if (isQSelected) AmberGold else TextSecondary, modifier = Modifier.size(16.dp))
                                                    Text(text = q.labelFa, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                                }
                                                Text(text = q.approxBitrate, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Download Progress Bar
                    if (isDownloadingWebVideo) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "در حال دریافت و آماده‌سازی ویدیو با کیفیت ${selectedQuality.resolution}...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = CyanNeon)
                                )
                                Text(
                                    text = "$downloadProgress%",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
                                )
                            }
                            LinearProgressIndicator(
                                progress = { downloadProgress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CyanNeon,
                                trackColor = DarkSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Section 2: Project Title
            Text(
                text = "۲. عنوان پروژه دوبله",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("نام پروژه برای ذخیره در استودیو") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("project_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedLabelColor = CyanNeon,
                    unfocusedLabelColor = TextSecondary,
                    focusedContainerColor = DarkSurfaceCard,
                    unfocusedContainerColor = DarkSurfaceCard
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Section 3: Dubbing Tone
            Text(
                text = "۳. لحن ترجمه و دوبله هوش مصنوعی",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DubbingTone.values().forEach { tone ->
                    val isSelected = selectedTone == tone
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) PurpleNeon.copy(alpha = 0.15f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) PurpleNeon else DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedTone = tone }
                            .testTag("tone_option_${tone.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) PurpleNeon else Color.Transparent)
                                    .border(1.5.dp, if (isSelected) PurpleNeon else DarkBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = DarkBackground, modifier = Modifier.size(16.dp))
                                }
                            }
                            Column {
                                Text(text = tone.titleFa, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text(text = tone.promptInstruction, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp))
                            }
                        }
                    }
                }
            }

            // Section 4: Voice Actor Selection
            Text(
                text = "۴. انتخاب صداپیشه و شبیه‌سازی صدا",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = CyanNeon)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceActor.values().forEach { actor ->
                    val isSelected = selectedVoiceActor == actor
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) CyanNeon.copy(alpha = 0.15f) else DarkSurfaceCard,
                        border = BorderStroke(1.dp, if (isSelected) CyanNeon else DarkBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVoiceActor = actor }
                            .testTag("voice_actor_${actor.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (actor.genderFa == "مرد") CyanNeon.copy(alpha = 0.2f) else PurpleNeon.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = if (actor.genderFa == "مرد") CyanNeon else PurpleNeon, modifier = Modifier.size(20.dp))
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = actor.titleFa, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text(text = actor.sampleDesc, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp))
                            }

                            IconButton(
                                onClick = {
                                    onPreviewVoice("سلام! من با صدای ${actor.titleFa} این ویدیو رو براتون دوبله می‌کنم.")
                                },
                                modifier = Modifier.testTag("test_voice_${actor.id}")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "تست صدا", tint = CyanNeon)
                            }
                        }
                    }
                }
            }

            // Section 5: Lip-Sync Switch
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, if (enableLipSync) AmberGold.copy(alpha = 0.5f) else DarkBorder),
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(AmberGold.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Face, contentDescription = null, tint = AmberGold)
                        }
                        Column {
                            Text(text = "همگام‌سازی حرکات لب (Lip-Sync)", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                            Text(text = "تطبیق فریم به فریم دهان با واج‌های فارسی (Sync Labs)", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                        }
                    }

                    Switch(
                        checked = enableLipSync,
                        onCheckedChange = { enableLipSync = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AmberGold,
                            checkedTrackColor = AmberGold.copy(alpha = 0.3f),
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = DarkSurfaceVariant
                        ),
                        modifier = Modifier.testTag("lip_sync_toggle")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Start Pipeline Button
            Button(
                onClick = {
                    if (selectedSourceTabIndex == 1 && inspectedWebVideo != null) {
                        onStartDubbingFromWeb(inspectedWebVideo, selectedQuality, selectedTone, selectedVoiceActor.id, enableLipSync)
                    } else if (selectedSourceTabIndex == 1) {
                        // Inspect first if not inspected
                        onInspectWebUrl(inputUrl)
                    } else {
                        selectedVideoUri?.let { uri ->
                            onStartDubbing(title, uri, selectedTone, selectedVoiceActor.id, enableLipSync)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("start_dubbing_process_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedSourceTabIndex == 1) PurpleNeon else CyanNeon,
                    contentColor = DarkBackground
                )
            ) {
                Icon(
                    imageVector = if (selectedSourceTabIndex == 1) Icons.Default.CloudDownload else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = DarkBackground
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedSourceTabIndex == 1) "دانلود ویدیو و شروع دوبله به فارسی" else "شروع خط لوله دوبله و هماهنگ‌سازی",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuickUrlChip(
    title: String,
    url: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceCard,
        border = BorderStroke(1.dp, DarkBorder),
        modifier = modifier.clickable { onClick(url) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.OndemandVideo, contentDescription = null, tint = RoseError, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary), maxLines = 1)
        }
    }
}
