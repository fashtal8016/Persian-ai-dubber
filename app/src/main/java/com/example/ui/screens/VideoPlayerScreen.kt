package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.ClosedCaptionOff
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.data.model.DubbingProject
import com.example.data.model.SubtitleSegment
import com.example.data.model.SubtitleStyleConfig
import com.example.ui.components.PersianSubtitleOverlay
import com.example.ui.theme.CyanNeon
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.GlassBackground
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassSurface
import com.example.ui.theme.PurpleNeon
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AudioTrackMode(val titleFa: String) {
    PERSIAN_DUB("دوبله فارسی"),
    ORIGINAL_AUDIO("صدای اصلی"),
    DUCKED_BOTH("ترکیب هوشمند (دوگانه)")
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    project: DubbingProject?,
    subtitles: List<SubtitleSegment>,
    subtitleStyle: SubtitleStyleConfig,
    onNavigateBack: () -> Unit,
    onOpenSubtitleEditor: () -> Unit,
    onOpenSubtitleStyling: () -> Unit,
    onUpdateSubtitleStyle: (SubtitleStyleConfig) -> Unit
) {
    BackHandler { onNavigateBack() }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(project?.durationMs ?: 26000L) }
    var areControlsVisible by remember { mutableStateOf(true) }
    var activeAudioTrack by remember { mutableStateOf(AudioTrackMode.PERSIAN_DUB) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Initialize ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            repeatMode = Player.REPEAT_MODE_ALL
            playWhenReady = true
        }
    }

    // Set video source
    LaunchedEffect(project?.sourceVideoUri) {
        val uriStr = project?.outputVideoUri ?: project?.sourceVideoUri
        if (!uriStr.isNullOrBlank()) {
            val mediaItem = MediaItem.fromUri(Uri.parse(uriStr))
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
            isPlaying = true
        }
    }

    // Progress polling loop for smooth subtitle matching and seekbar
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            currentPositionMs = exoPlayer.currentPosition
            if (exoPlayer.duration > 0) {
                durationMs = exoPlayer.duration
            }
            delay(100)
        }
    }

    // Auto hide controls after 4 seconds of inactivity
    LaunchedEffect(areControlsVisible) {
        if (areControlsVisible && isPlaying) {
            delay(4000)
            areControlsVisible = false
        }
    }

    DisposableEffect(Unit) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && exoPlayer.duration > 0) {
                    durationMs = exoPlayer.duration
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Find the active subtitle line matching currentPositionMs
    val currentSubtitleText = remember(currentPositionMs, subtitles) {
        derivedStateOf {
            val matchingSegment = subtitles.find {
                currentPositionMs >= it.startMs && currentPositionMs <= it.endMs
            }
            matchingSegment?.translatedText ?: ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                areControlsVisible = !areControlsVisible
            }
    ) {
        // Video Surface
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    keepScreenOn = true
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Custom Persian Subtitle Overlay Layer
        PersianSubtitleOverlay(
            subtitleText = currentSubtitleText.value,
            styleConfig = subtitleStyle,
            onPositionChanged = { newRatio ->
                onUpdateSubtitleStyle(subtitleStyle.copy(verticalOffsetFraction = newRatio))
            }
        )

        // Glassmorphic Player Controls Overlay
        AnimatedVisibility(
            visible = areControlsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC000000),
                                Color.Transparent,
                                Color.Transparent,
                                Color(0xEE000000)
                            )
                        )
                    )
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                        .align(Alignment.TopCenter),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassSurface)
                                .testTag("player_back_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت", tint = TextPrimary)
                        }

                        Text(
                            text = project?.title ?: "پخش ویدیو",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            ),
                            maxLines = 1
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Quick Subtitle Toggle
                        IconButton(
                            onClick = {
                                onUpdateSubtitleStyle(subtitleStyle.copy(isVisible = !subtitleStyle.isVisible))
                            },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (subtitleStyle.isVisible) CyanNeon.copy(alpha = 0.2f) else GlassSurface)
                                .testTag("quick_toggle_subtitles_button")
                        ) {
                            Icon(
                                imageVector = if (subtitleStyle.isVisible) Icons.Default.ClosedCaption else Icons.Default.ClosedCaptionOff,
                                contentDescription = "زیرنویس",
                                tint = if (subtitleStyle.isVisible) CyanNeon else TextSecondary
                            )
                        }

                        // Subtitle Styling Button
                        IconButton(
                            onClick = onOpenSubtitleStyling,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassSurface)
                                .testTag("player_subtitle_styling_button")
                        ) {
                            Icon(Icons.Default.Style, contentDescription = "تنظیمات ظاهر زیرنویس", tint = TextPrimary)
                        }

                        // Subtitle Text Editor Button
                        IconButton(
                            onClick = onOpenSubtitleEditor,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(GlassSurface)
                                .testTag("player_subtitle_editor_button")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "ویرایش زیرنویس", tint = TextPrimary)
                        }
                    }
                }

                // Center Controls (Play/Pause, Rewind 10, Fast Forward 10)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .testTag("rewind_10s_button")
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "-10 ثانیه", tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }

                    IconButton(
                        onClick = {
                            if (isPlaying) {
                                exoPlayer.pause()
                                isPlaying = false
                            } else {
                                exoPlayer.play()
                                isPlaying = true
                            }
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(CyanNeon)
                            .testTag("play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "توقف" else "پخش",
                            tint = DarkBackground,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(durationMs)
                            exoPlayer.seekTo(newPos)
                            currentPositionMs = newPos
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(GlassSurface)
                            .testTag("forward_10s_button")
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "+10 ثانیه", tint = TextPrimary, modifier = Modifier.size(28.dp))
                    }
                }

                // Bottom Panel (Audio Track Pills, Seekbar, Time, Speed)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Audio Track Selector Glass Bar
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GlassSurface,
                        border = BorderStroke(1.dp, GlassBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                                Text(text = "کانال صوتی:", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                AudioTrackMode.values().forEach { mode ->
                                    val isSelected = activeAudioTrack == mode
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) CyanNeon.copy(alpha = 0.25f) else Color.Transparent,
                                        border = if (isSelected) BorderStroke(1.dp, CyanNeon) else null,
                                        modifier = Modifier.clickable { activeAudioTrack = mode }
                                    ) {
                                        Text(
                                            text = mode.titleFa,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            ),
                                            color = if (isSelected) CyanNeon else TextSecondary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Seekbar Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = SubtitleSegment.formatMsToTime(currentPositionMs),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                        )

                        Slider(
                            value = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f,
                            onValueChange = { fraction ->
                                val targetMs = (fraction * durationMs).toLong()
                                currentPositionMs = targetMs
                                exoPlayer.seekTo(targetMs)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = CyanNeon,
                                activeTrackColor = CyanNeon,
                                inactiveTrackColor = DarkSurfaceVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("video_player_seekbar")
                        )

                        Text(
                            text = SubtitleSegment.formatMsToTime(durationMs),
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        // Playback Speed Button
                        Box {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GlassSurface,
                                border = BorderStroke(1.dp, GlassBorder),
                                modifier = Modifier.clickable { showSpeedMenu = true }
                            ) {
                                Text(
                                    text = "${playbackSpeed}x",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = TextPrimary),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                                    DropdownMenuItem(
                                        text = { Text("${speed}x", color = if (playbackSpeed == speed) CyanNeon else TextPrimary) },
                                        onClick = {
                                            playbackSpeed = speed
                                            exoPlayer.setPlaybackSpeed(speed)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
