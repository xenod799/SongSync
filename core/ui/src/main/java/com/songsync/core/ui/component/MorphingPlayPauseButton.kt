package com.songsync.core.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.theme.SongSyncMotion
import com.songsync.core.ui.theme.bouncyClickable
import com.songsync.core.ui.theme.isAnimationDisabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Shape that interpolates between Cookie9Sided (fraction = 0f) and Circle (fraction = 1f)
 */
class MorphingPlayPauseShape(private val progress: Float) : Shape {
    override fun createOutline(size: androidx.compose.ui.geometry.Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val numLobes = 9
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val outerRadius = minOf(centerX, centerY)

        // As progress goes from 0f (paused) to 1f (playing), innerRadius approaches outerRadius (circle)
        val innerRatio = 0.84f + (0.16f * progress)
        val innerRadius = outerRadius * innerRatio

        val totalPoints = numLobes * 2
        val angleStep = (2.0 * PI / totalPoints)

        for (i in 0 until totalPoints) {
            val radius = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * angleStep - PI / 2
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun MorphingPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    val noAnim = isAnimationDisabled()

    // 0f = paused (Cookie9Sided), 1f = playing (Circle)
    val shapeProgress by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0f,
        animationSpec = if (noAnim) SongSyncMotion.EffectsFast else SongSyncMotion.SpatialDefault,
        label = "shapeMorph"
    )

    val currentShape = MorphingPlayPauseShape(shapeProgress)

    Box(
        modifier = modifier
            .size(size)
            .bouncyClickable(onClick = onClick)
            .clip(currentShape)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(
            targetState = isPlaying,
            animationSpec = if (noAnim) SongSyncMotion.EffectsFast else SongSyncMotion.EffectsDefault,
            label = "iconCrossfade"
        ) { playing ->
            if (playing) {
                Icon(
                    imageVector = Icons.Rounded.Pause,
                    contentDescription = "Pause",
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.48f)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = contentColor,
                    modifier = Modifier.size(size * 0.52f)
                )
            }
        }
    }
}
