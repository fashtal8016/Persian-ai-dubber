package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DubbingProject
import com.example.data.model.ProjectStatus
import com.example.data.model.SubtitleSegment
import com.example.ui.ProjectFilter
import com.example.ui.Screen
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CyanGlow
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
import com.example.ui.theme.VioletAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    projects: List<DubbingProject>,
    activeFilter: ProjectFilter,
    isOfflineMode: Boolean,
    userMessage: String?,
    onFilterChanged: (ProjectFilter) -> Unit,
    onOpenWizard: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectProjectForPlay: (Long) -> Unit,
    onSelectProjectForSubtitles: (Long) -> Unit,
    onReRunDubbing: (Long) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onClearUserMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            onClearUserMessage()
        }
    }

    val filteredProjects = when (activeFilter) {
        ProjectFilter.ALL -> projects
        ProjectFilter.COMPLETED -> projects.filter { it.status == ProjectStatus.COMPLETED }
        ProjectFilter.PROCESSING -> projects.filter { it.status != ProjectStatus.COMPLETED && it.status != ProjectStatus.ERROR }
    }

    Scaffold(
        topBar = {
            StudioTopBar(
                title = "استودیو دوبله هوشمند فارسی",
                canNavigateBack = false,
                onNavigateBack = {},
                onOpenSettings = onOpenSettings,
                isOfflineMode = isOfflineMode
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenWizard,
                containerColor = CyanNeon,
                contentColor = DarkBackground,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "دوبله ویدیوی جدید"
                    )
                },
                text = {
                    Text(
                        text = "دوبله ویدیوی جدید",
                        fontWeight = FontWeight.Bold
                    )
                },
                modifier = Modifier.testTag("fab_new_dubbing")
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("dashboard_project_list"),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Hero Studio Header Card
            item {
                StudioHeroBanner(
                    completedCount = projects.count { it.status == ProjectStatus.COMPLETED },
                    processingCount = projects.count { it.status != ProjectStatus.COMPLETED && it.status != ProjectStatus.ERROR },
                    onStartDubbing = onOpenWizard
                )
            }

            // Filter Chips Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProjectFilter.values().forEach { filter ->
                        FilterChip(
                            selected = activeFilter == filter,
                            onClick = { onFilterChanged(filter) },
                            label = {
                                Text(
                                    text = filter.titleFa,
                                    fontWeight = if (activeFilter == filter) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon,
                                containerColor = DarkSurfaceCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = activeFilter == filter,
                                selectedBorderColor = CyanNeon,
                                borderColor = DarkBorder
                            )
                        )
                    }
                }
            }

            // Project Items
            if (filteredProjects.isEmpty()) {
                item {
                    EmptyProjectsState(onOpenWizard = onOpenWizard)
                }
            } else {
                items(
                    items = filteredProjects,
                    key = { it.id }
                ) { project ->
                    ProjectCardItem(
                        project = project,
                        onPlayClick = { onSelectProjectForPlay(project.id) },
                        onEditSubtitlesClick = { onSelectProjectForSubtitles(project.id) },
                        onReRunClick = { onReRunDubbing(project.id) },
                        onDeleteClick = { onDeleteProject(project.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioHeroBanner(
    completedCount: Int,
    processingCount: Int,
    onStartDubbing: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, Brush.horizontalGradient(listOf(CyanNeon.copy(alpha = 0.5f), PurpleNeon.copy(alpha = 0.5f))))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.radialGradient(
                        colors = listOf(PurpleNeon.copy(alpha = 0.15f), Color.Transparent),
                        radius = 800f
                    )
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "استودیوی دوبلاژ و Lip-Sync هوشمند",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "تبدیل خودکار ویدیوها به زبان فارسی با صدای طبیعی و همگام‌سازی حرکات لب",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(text = "$completedCount پروژه", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text(text = "آماده پخش", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurface,
                        border = BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(CyanNeon.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(text = "$processingCount در حال کار", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                                Text(text = "خط لوله AI", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectCardItem(
    project: DubbingProject,
    onPlayClick: () -> Unit,
    onEditSubtitlesClick: () -> Unit,
    onReRunClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isCompleted = project.status == ProjectStatus.COMPLETED
    val isProcessing = project.status != ProjectStatus.COMPLETED && project.status != ProjectStatus.ERROR

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = isCompleted, onClick = onPlayClick)
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        border = BorderStroke(1.dp, if (isCompleted) DarkBorder else CyanNeon.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title and Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = project.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مدت زمان: ${SubtitleSegment.formatMsToTime(project.durationMs)} • ${project.subtitleCount} خط زیرنویس",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }

                StatusBadge(status = project.status)
            }

            // Real-time processing indicator
            if (isProcessing) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = project.statusMessage.ifBlank { project.status.titleFa },
                            style = MaterialTheme.typography.bodySmall.copy(color = CyanNeon),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${project.progressPercent}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        )
                    }
                    LinearProgressIndicator(
                        progress = { project.progressPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = CyanNeon,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }

            // Tags row (Tone, Voice Actor, Lip Sync)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeTag(icon = Icons.Default.Mic, text = project.dubbingTone.titleFa, color = PurpleNeon)
                if (project.enableLipSync) {
                    BadgeTag(icon = Icons.Default.Face, text = "Lip-Sync فعال", color = AmberGold)
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.testTag("delete_project_${project.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف پروژه",
                        tint = TextSecondary
                    )
                }

                if (!isCompleted && !isProcessing) {
                    OutlinedButton(
                        onClick = onReRunClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AmberGold)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("تلاش مجدد")
                    }
                }

                if (isCompleted) {
                    OutlinedButton(
                        onClick = onEditSubtitlesClick,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                        border = BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.testTag("edit_subtitles_button_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Subtitles,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("زیرنویس و متن")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanNeon,
                            contentColor = DarkBackground
                        ),
                        modifier = Modifier.testTag("play_project_button_${project.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("پخش ویدیو", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ProjectStatus) {
    val (bg, text, textColor) = when (status) {
        ProjectStatus.COMPLETED -> Triple(EmeraldSuccess.copy(alpha = 0.15f), "آماده پخش", EmeraldSuccess)
        ProjectStatus.ERROR -> Triple(RoseError.copy(alpha = 0.15f), "خطا", RoseError)
        ProjectStatus.DRAFT -> Triple(TextSecondary.copy(alpha = 0.15f), "پیش‌نویس", TextSecondary)
        else -> Triple(CyanNeon.copy(alpha = 0.15f), status.titleFa, CyanNeon)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun BadgeTag(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = color
            )
        }
    }
}

@Composable
private fun EmptyProjectsState(onOpenWizard: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = CyanNeon,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = "هنوز ویدیویی برای دوبله اضافه نشده است",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Text(
            text = "یک فایل ویدیو از گالری انتخاب کنید یا یکی از ویدیوهای نمونه را دوبله نمایید.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextSecondary,
                lineHeight = 22.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Button(
            onClick = onOpenWizard,
            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBackground),
            modifier = Modifier.testTag("empty_state_new_dubbing_button")
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("شروع دوبله ویدیو", fontWeight = FontWeight.Bold)
        }
    }
}
