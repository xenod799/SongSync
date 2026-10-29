package com.songsync.core.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val SongSyncShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

val PillShape = CircleShape
val CardContainerShape = RoundedCornerShape(28.dp)
val ListItemShape = RoundedCornerShape(24.dp)
val BottomSheetTopShape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp)
val ListArtShape = RoundedCornerShape(16.dp)
val PlayerArtDefaultShape = RoundedCornerShape(36.dp)

/**
 * 9-sided Scalloped Cookie shape corresponding to MaterialShapes.Cookie9Sided
 */
class Cookie9SidedShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val numLobes = 9
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val outerRadius = minOf(centerX, centerY)
        val innerRadius = outerRadius * 0.85f

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

/**
 * Soft Burst shape with smooth outward curvature
 */
class SoftBurstShape : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val numPoints = 8
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val maxR = minOf(centerX, centerY)
        val minR = maxR * 0.78f
        val step = (2.0 * PI / (numPoints * 2))

        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) maxR else minR
            val angle = i * step
            val x = centerX + r * cos(angle).toFloat()
            val y = centerY + r * sin(angle).toFloat()

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
