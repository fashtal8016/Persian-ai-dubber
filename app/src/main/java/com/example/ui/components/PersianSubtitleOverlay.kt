package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SubtitleStyleConfig
import com.example.ui.theme.VazirmatnFontFamily
import kotlin.math.roundToInt

@Composable
fun PersianSubtitleOverlay(
    subtitleText: String,
    styleConfig: SubtitleStyleConfig,
    modifier: Modifier = Modifier,
    onPositionChanged: ((Float) -> Unit)? = null
) {
    if (!styleConfig.isVisible || subtitleText.isBlank()) return

    val fontFamily = when (styleConfig.fontChoice) {
        "Vazirmatn" -> VazirmatnFontFamily
        "SansSerif" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val totalHeightPx = constraints.maxHeight.toFloat()
        var currentOffsetFraction by remember(styleConfig.verticalOffsetFraction) {
            mutableFloatStateOf(styleConfig.verticalOffsetFraction)
        }

        val yPositionPx = totalHeightPx * currentOffsetFraction

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(0, (yPositionPx - 40).roundToInt().coerceIn(0, (totalHeightPx - 80).toInt())) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val newY = (yPositionPx + dragAmount.y).coerceIn(40f, totalHeightPx - 80f)
                        val fraction = newY / totalHeightPx
                        currentOffsetFraction = fraction
                        onPositionChanged?.invoke(fraction)
                    }
                }
                .padding(horizontal = 24.dp)
                .testTag("persian_subtitle_overlay_container"),
            contentAlignment = Alignment.Center
        ) {
            val hasBackground = (styleConfig.backgroundColor and 0xFF000000L) != 0L

            Box(
                modifier = Modifier
                    .then(
                        if (hasBackground) {
                            Modifier
                                .clip(RoundedCornerShape(styleConfig.backgroundCornerRadiusDp.dp))
                                .background(styleConfig.composeBackgroundColor())
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        } else {
                            Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        }
                    )
            ) {
                // Stroke effect via multi-directional shadows if strokeWidth > 0
                val shadow = if (styleConfig.strokeWidth > 0f) {
                    Shadow(
                        color = styleConfig.composeStrokeColor(),
                        offset = Offset(0f, 2f),
                        blurRadius = styleConfig.strokeWidth * 2f
                    )
                } else null

                Text(
                    text = subtitleText,
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontWeight = if (styleConfig.isBold) FontWeight.Bold else FontWeight.Normal,
                        fontSize = styleConfig.fontSizeSp.sp,
                        color = styleConfig.composeTextColor(),
                        textAlign = TextAlign.Center,
                        lineHeight = (styleConfig.fontSizeSp * 1.35f).sp,
                        shadow = shadow
                    ),
                    modifier = Modifier.testTag("persian_subtitle_text")
                )
            }
        }
    }
}
