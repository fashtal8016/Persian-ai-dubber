package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DubbingProject
import com.example.data.model.SubtitleSegment
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.RoseError
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VazirmatnFontFamily

@Composable
fun SubtitleEditorScreen(
    project: DubbingProject?,
    subtitles: List<SubtitleSegment>,
    onNavigateBack: () -> Unit,
    onUpdateSubtitleText: (Long, String) -> Unit,
    onDeleteSubtitle: (Long) -> Unit,
    onPreviewVoice: (String) -> Unit,
    onExportSrt: (Long) -> Unit
) {
    BackHandler { onNavigateBack() }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "ویرایشگر زیرنویس فارسی (SRT)",
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
        ) {
            // Header stats & export SRT action
            Surface(
                color = DarkSurfaceCard,
                border = BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = project?.title ?: "پروژه",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                        )
                        Text(
                            text = "${subtitles.size} خط زیرنویس همگام با زمان ویدیو",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }

                    Button(
                        onClick = {
                            project?.id?.let { pId ->
                                onExportSrt(pId)
                                Toast.makeText(context, "فایل SRT با موفقیت ذخیره شد", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBackground),
                        modifier = Modifier.testTag("export_srt_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خروجی SRT", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Subtitle Segments List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(
                    items = subtitles,
                    key = { it.id }
                ) { segment ->
                    SubtitleSegmentEditorItem(
                        segment = segment,
                        onTextChange = { newText ->
                            onUpdateSubtitleText(segment.id, newText)
                        },
                        onDelete = {
                            onDeleteSubtitle(segment.id)
                        },
                        onPreviewVoice = {
                            onPreviewVoice(segment.translatedText)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubtitleSegmentEditorItem(
    segment: SubtitleSegment,
    onTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onPreviewVoice: () -> Unit
) {
    var editText by remember(segment.translatedText) { mutableStateOf(segment.translatedText) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, if (segment.isEditedByUser) CyanNeon.copy(alpha = 0.4f) else DarkBorder),
        modifier = Modifier.fillMaxWidth().testTag("subtitle_item_${segment.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Timestamp and index bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = CyanNeon.copy(alpha = 0.15f),
                        border = BorderStroke(0.5.dp, CyanNeon.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "#${segment.orderIndex + 1}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CyanNeon,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Text(
                        text = "${segment.formattedStartTime()} ◄ ${segment.formattedEndTime()}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Voice preview button
                    IconButton(
                        onClick = onPreviewVoice,
                        modifier = Modifier.size(32.dp).testTag("preview_voice_${segment.id}")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "تست صدا", tint = PurpleNeon, modifier = Modifier.size(18.dp))
                    }

                    // Delete segment button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_subtitle_${segment.id}")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف خط", tint = TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Original English Line
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = segment.originalText,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Editable Persian Line
            OutlinedTextField(
                value = editText,
                onValueChange = {
                    editText = it
                    onTextChange(it)
                },
                label = { Text("متن دوبله و زیرنویس فارسی") },
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = VazirmatnFontFamily,
                    color = TextPrimary,
                    lineHeight = 22.sp
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = DarkBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = DarkBackground,
                    unfocusedContainerColor = DarkBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("edit_subtitle_text_${segment.id}")
            )
        }
    }
}
