package com.songsync.core.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ExpressiveMusicNotePlaceholder(
    modifier: Modifier = Modifier,
    iconSize: Dp? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val tertiaryContainer = MaterialTheme.colorScheme.tertiaryContainer
    val onContainer = MaterialTheme.colorScheme.onPrimaryContainer

    val backgroundBrush = Brush.linearGradient(
        colors = listOf(primaryContainer, tertiaryContainer)
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        val noteModifier = if (iconSize != null) Modifier.size(iconSize) else Modifier.fillMaxSize(0.55f)

        Canvas(modifier = noteModifier) {
            val w = size.width
            val h = size.height
            val scale = w / 100f

            // Refined Beamed Double Eighth-Notes
            val noteHeadRadiusX = 13f * scale
            val noteHeadRadiusY = 10f * scale

            val leftHeadCenter = Offset(30f * scale, 75f * scale)
            val rightHeadCenter = Offset(70f * scale, 65f * scale)

            val stemWidth = 6f * scale
            val leftStemX = leftHeadCenter.x + noteHeadRadiusX - stemWidth
            val rightStemX = rightHeadCenter.x + noteHeadRadiusX - stemWidth

            val leftStemTop = 26f * scale
            val rightStemTop = 16f * scale

            // Left Notehead (tilted musical ellipse)
            drawOval(
                color = onContainer,
                topLeft = Offset(leftHeadCenter.x - noteHeadRadiusX, leftHeadCenter.y - noteHeadRadiusY),
                size = androidx.compose.ui.geometry.Size(noteHeadRadiusX * 2, noteHeadRadiusY * 2)
            )

            // Right Notehead (tilted musical ellipse)
            drawOval(
                color = onContainer,
                topLeft = Offset(rightHeadCenter.x - noteHeadRadiusX, rightHeadCenter.y - noteHeadRadiusY),
                size = androidx.compose.ui.geometry.Size(noteHeadRadiusX * 2, noteHeadRadiusY * 2)
            )

            // Left Stem
            drawRoundRect(
                color = onContainer,
                topLeft = Offset(leftStemX, leftStemTop),
                size = androidx.compose.ui.geometry.Size(stemWidth, leftHeadCenter.y - leftStemTop),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )

            // Right Stem
            drawRoundRect(
                color = onContainer,
                topLeft = Offset(rightStemX, rightStemTop),
                size = androidx.compose.ui.geometry.Size(stemWidth, rightHeadCenter.y - rightStemTop),
                cornerRadius = CornerRadius(3f * scale, 3f * scale)
            )

            // Connecting Beam at the Top
            val beamPath = Path().apply {
                moveTo(leftStemX, leftStemTop)
                lineTo(rightStemX + stemWidth, rightStemTop)
                lineTo(rightStemX + stemWidth, rightStemTop + (10f * scale))
                lineTo(leftStemX, leftStemTop + (10f * scale))
                close()
            }
            drawPath(path = beamPath, color = onContainer)

            // Subtle Musical Harmony Ripple / Wave
            drawArc(
                color = primaryColor.copy(alpha = 0.4f),
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(rightStemX + (8f * scale), rightStemTop - (6f * scale)),
                size = androidx.compose.ui.geometry.Size(20f * scale, 20f * scale),
                style = Stroke(width = 2.5f * scale, cap = StrokeCap.Round)
            )
        }
    }
}
