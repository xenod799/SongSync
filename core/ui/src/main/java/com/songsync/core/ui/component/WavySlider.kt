package com.songsync.core.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.theme.SongSyncMotion
import com.songsync.core.ui.theme.isAnimationDisabled
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavySlider(
    value: Float, // 0f to 1f
    onValueChange: (Float) -> Unit,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    waveLengthDp: Float = 40f,
    playingAmplitudeDp: Float = 8f,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val noAnim = isAnimationDisabled()

    val targetAmplitude = if (isPlaying) playingAmplitudeDp else 0f
    val animatedAmplitude by animateFloatAsState(
        targetValue = targetAmplitude,
        animationSpec = if (noAnim) SongSyncMotion.EffectsFast else SongSyncMotion.SpatialSlow,
        label = "waveAmplitude"
    )

    val waveLengthPx = with(density) { waveLengthDp.dp.toPx() }
    val amplitudePx = with(density) { animatedAmplitude.dp.toPx() }
    val strokeWidthPx = with(density) { 4.dp.toPx() }
    val thumbRadiusPx = with(density) { 10.dp.toPx() }

    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(value) }

    val currentProgress = if (isDragging) dragProgress else value.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onValueChange(newProgress)
                        onValueChangeFinished?.invoke()
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        dragProgress = (offset.x / size.width).coerceIn(0f, 1f)
                        onValueChange(dragProgress)
                    },
                    onDragEnd = {
                        isDragging = false
                        onValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                    },
                    onHorizontalDrag = { change, _ ->
                        dragProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                        onValueChange(dragProgress)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val centerY = size.height / 2f
            val progressWidth = size.width * currentProgress

            // 1. Inactive Track (flat line)
            drawLine(
                color = inactiveColor,
                start = Offset(progressWidth, centerY),
                end = Offset(size.width, centerY),
                strokeWidth = strokeWidthPx,
                cap = StrokeCap.Round
            )

            // 2. Active Wave Track
            if (progressWidth > 0f) {
                val wavePath = Path()
                wavePath.moveTo(0f, centerY)

                if (amplitudePx > 0.1f) {
                    var x = 0f
                    val step = 4f
                    while (x <= progressWidth) {
                        val phase = (x / waveLengthPx) * 2.0 * PI
                        val y = centerY + (amplitudePx * sin(phase)).toFloat()
                        wavePath.lineTo(x, y)
                        x += step
                    }
                    wavePath.lineTo(progressWidth, centerY)
                } else {
                    wavePath.lineTo(progressWidth, centerY)
                }

                drawPath(
                    path = wavePath,
                    color = activeColor,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )

                // 3. Playful Thumb Indicator
                drawCircle(
                    color = activeColor,
                    radius = thumbRadiusPx,
                    center = Offset(progressWidth, centerY)
                )
                drawCircle(
                    color = Color.White,
                    radius = thumbRadiusPx * 0.45f,
                    center = Offset(progressWidth, centerY)
                )
            }
        }
    }
}
