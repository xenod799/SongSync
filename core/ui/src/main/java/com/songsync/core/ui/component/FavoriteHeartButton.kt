package com.songsync.core.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.theme.LocalSongSyncExtras
import com.songsync.core.ui.theme.bouncyClickable
import com.songsync.core.ui.theme.isAnimationDisabled
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FavoriteHeartButton(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 26.dp
) {
    val butterColor = LocalSongSyncExtras.current.butter
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
    val noAnim = isAnimationDisabled()

    val scaleAnim = remember { Animatable(1f) }
    val sparklesAnim = remember { Animatable(0f) }

    LaunchedEffect(isFavorite) {
        if (isFavorite && !noAnim) {
            // Scale pop: 1.0 -> 1.35 -> 1.0 with fast bouncy spring
            scaleAnim.animateTo(
                targetValue = 1.35f,
                animationSpec = spring(dampingRatio = 0.4f, stiffness = 900f)
            )
            scaleAnim.animateTo(
                targetValue = 1.0f,
                animationSpec = spring(dampingRatio = 0.5f, stiffness = 600f)
            )
        } else {
            scaleAnim.snapTo(1f)
        }
    }

    LaunchedEffect(isFavorite) {
        if (isFavorite && !noAnim) {
            sparklesAnim.snapTo(0f)
            sparklesAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f)
            )
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .bouncyClickable(onClick = onToggleFavorite),
        contentAlignment = Alignment.Center
    ) {
        // 6 butter-colored sparkles radiating outward and fading
        if (sparklesAnim.value > 0f && sparklesAnim.value < 1f) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val sparkProgress = sparklesAnim.value
                val maxRadius = this.size.width * 0.45f
                val distance = maxRadius * sparkProgress
                val alpha = (1f - sparkProgress).coerceIn(0f, 1f)

                for (i in 0 until 6) {
                    val angle = (i * (2 * PI / 6)) - PI / 2
                    val x = center.x + (distance * cos(angle)).toFloat()
                    val y = center.y + (distance * sin(angle)).toFloat()
                    val sparkSize = (3.5.dp.toPx() * (1f - sparkProgress * 0.5f))

                    drawCircle(
                        color = butterColor.copy(alpha = alpha),
                        radius = sparkSize,
                        center = Offset(x, y)
                    )
                }
            }
        }

        Icon(
            imageVector = if (isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = if (isFavorite) butterColor else inactiveColor,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
        )
    }
}
