package com.songsync.core.ui.mascot

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.theme.LocalSongSyncExtras
import com.songsync.core.ui.theme.isAnimationDisabled

enum class SyncPose {
    HAPPY,
    SLEEPY,
    SEARCHING
}

@Composable
fun SyncMascot(
    pose: SyncPose = SyncPose.HAPPY,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val bodyColor = MaterialTheme.colorScheme.primaryContainer
    val outlineColor = MaterialTheme.colorScheme.primary
    val faceColor = MaterialTheme.colorScheme.onPrimaryContainer
    val blushColor = LocalSongSyncExtras.current.blush
    val noAnim = isAnimationDisabled()

    val infiniteTransition = rememberInfiniteTransition(label = "mascot")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floating"
    )

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val scale = w / 100f
        val animatedY = if (noAnim) 0f else floatOffset * scale

        // 1. Draw musical note stem and head (body)
        val headCenter = Offset(38f * scale, (68f * scale) + animatedY)

        val notePath = Path().apply {
            // Note head (proper musical tilted ellipse)
            addOval(
                androidx.compose.ui.geometry.Rect(
                    left = headCenter.x - (21f * scale),
                    top = headCenter.y - (16f * scale),
                    right = headCenter.x + (21f * scale),
                    bottom = headCenter.y + (16f * scale)
                )
            )
            // Stem
            val stemWidth = 7f * scale
            val stemLeft = headCenter.x + (14f * scale)
            val stemTop = (20f * scale) + animatedY
            val stemBottom = headCenter.y
            addRoundRect(
                RoundRect(
                    left = stemLeft,
                    top = stemTop,
                    right = stemLeft + stemWidth,
                    bottom = stemBottom,
                    cornerRadius = CornerRadius(4f * scale, 4f * scale)
                )
            )
            // Flag at the top
            moveTo(stemLeft + stemWidth, stemTop)
            cubicTo(
                stemLeft + (18f * scale), stemTop + (4f * scale),
                stemLeft + (28f * scale), stemTop + (14f * scale),
                stemLeft + (24f * scale), stemTop + (28f * scale)
            )
            cubicTo(
                stemLeft + (22f * scale), stemTop + (20f * scale),
                stemLeft + (14f * scale), stemTop + (12f * scale),
                stemLeft + stemWidth, stemTop + (10f * scale)
            )
            close()
        }

        // Fill body and draw gentle outline
        drawPath(path = notePath, color = bodyColor)
        drawPath(path = notePath, color = outlineColor.copy(alpha = 0.35f), style = Stroke(width = 2.5f * scale))

        // 2. Draw Cheeks (Blush)
        val leftCheek = Offset(headCenter.x - (9f * scale), headCenter.y + (3f * scale))
        val rightCheek = Offset(headCenter.x + (9f * scale), headCenter.y + (3f * scale))
        drawCircle(color = blushColor, radius = 3.8f * scale, center = leftCheek)
        drawCircle(color = blushColor, radius = 3.8f * scale, center = rightCheek)

        // 3. Draw Face Poses
        when (pose) {
            SyncPose.HAPPY -> {
                // Happy curved eyes (^ ^)
                val eyeWidth = 4f * scale
                val leftEyePath = Path().apply {
                    moveTo(headCenter.x - (8f * scale), headCenter.y - (2f * scale))
                    quadraticTo(
                        headCenter.x - (6f * scale), headCenter.y - (5f * scale),
                        headCenter.x - (4f * scale), headCenter.y - (2f * scale)
                    )
                }
                val rightEyePath = Path().apply {
                    moveTo(headCenter.x + (4f * scale), headCenter.y - (2f * scale))
                    quadraticTo(
                        headCenter.x + (6f * scale), headCenter.y - (5f * scale),
                        headCenter.x + (8f * scale), headCenter.y - (2f * scale)
                    )
                }
                drawPath(leftEyePath, color = faceColor, style = Stroke(width = 2f * scale, cap = StrokeCap.Round))
                drawPath(rightEyePath, color = faceColor, style = Stroke(width = 2f * scale, cap = StrokeCap.Round))

                // Cheerful open smile
                val mouthPath = Path().apply {
                    moveTo(headCenter.x - (4f * scale), headCenter.y + (2f * scale))
                    quadraticTo(
                        headCenter.x, headCenter.y + (7f * scale),
                        headCenter.x + (4f * scale), headCenter.y + (2f * scale)
                    )
                }
                drawPath(mouthPath, color = faceColor, style = Stroke(width = 2f * scale, cap = StrokeCap.Round))
            }
            SyncPose.SLEEPY -> {
                // Resting closed eyes (- -)
                drawLine(
                    color = faceColor,
                    start = Offset(headCenter.x - (8f * scale), headCenter.y - (3f * scale)),
                    end = Offset(headCenter.x - (4f * scale), headCenter.y - (3f * scale)),
                    strokeWidth = 2f * scale,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = faceColor,
                    start = Offset(headCenter.x + (4f * scale), headCenter.y - (3f * scale)),
                    end = Offset(headCenter.x + (8f * scale), headCenter.y - (3f * scale)),
                    strokeWidth = 2f * scale,
                    cap = StrokeCap.Round
                )
                // Small serene resting mouth
                drawCircle(color = faceColor, radius = 1.2f * scale, center = Offset(headCenter.x, headCenter.y + (3f * scale)))

                // Floating sleepy Z bubbles
                val zColor = outlineColor.copy(alpha = 0.6f)
                drawLine(zColor, Offset(headCenter.x + (16f * scale), headCenter.y - (12f * scale)), Offset(headCenter.x + (20f * scale), headCenter.y - (12f * scale)), 1.5f * scale)
                drawLine(zColor, Offset(headCenter.x + (20f * scale), headCenter.y - (12f * scale)), Offset(headCenter.x + (16f * scale), headCenter.y - (8f * scale)), 1.5f * scale)
                drawLine(zColor, Offset(headCenter.x + (16f * scale), headCenter.y - (8f * scale)), Offset(headCenter.x + (20f * scale), headCenter.y - (8f * scale)), 1.5f * scale)
            }
            SyncPose.SEARCHING -> {
                // Wide curious eyes
                drawCircle(color = faceColor, radius = 2.2f * scale, center = Offset(headCenter.x - (6f * scale), headCenter.y - (3f * scale)))
                drawCircle(color = faceColor, radius = 2.2f * scale, center = Offset(headCenter.x + (6f * scale), headCenter.y - (3f * scale)))

                // Curious "o" mouth
                drawCircle(color = faceColor, radius = 1.8f * scale, center = Offset(headCenter.x, headCenter.y + (3f * scale)), style = Stroke(1.5f * scale))

                // Cute magnifying glass held up
                val glassCenter = Offset(headCenter.x + (18f * scale), headCenter.y - (6f * scale))
                val glassRadius = 6f * scale
                drawCircle(color = outlineColor, radius = glassRadius, center = glassCenter, style = Stroke(2f * scale))
                drawLine(
                    color = outlineColor,
                    start = Offset(glassCenter.x + (4.5f * scale), glassCenter.y + (4.5f * scale)),
                    end = Offset(glassCenter.x + (10f * scale), glassCenter.y + (10f * scale)),
                    strokeWidth = 2.5f * scale,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
