package com.songsync.core.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.animation.core.animateFloatAsState

object SongSyncMotion {
    // Spatial Springs
    val SpatialFast = spring<Float>(dampingRatio = 0.6f, stiffness = 800f)
    val SpatialDefault = spring<Float>(dampingRatio = 0.8f, stiffness = 380f)
    val SpatialSlow = spring<Float>(dampingRatio = 0.8f, stiffness = 200f)

    // Effects Springs
    val EffectsFast = spring<Float>(dampingRatio = 1.0f, stiffness = 3800f)
    val EffectsDefault = spring<Float>(dampingRatio = 1.0f, stiffness = 1600f)
    val EffectsSlow = spring<Float>(dampingRatio = 1.0f, stiffness = 800f)

    val PressSpring = spring<Float>(dampingRatio = 0.5f, stiffness = 400f)
}

@Composable
fun isAnimationDisabled(): Boolean {
    val context = LocalContext.current
    return try {
        val durationScale = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1.0f
        )
        durationScale == 0f
    } catch (_: Exception) {
        false
    }
}

/**
 * Modifier for playful tactile press feedback: shrinks to 0.92 scale with bouncy spring and light haptic
 */
fun Modifier.bouncyClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val noAnim = isAnimationDisabled()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.92f else 1.0f,
        animationSpec = if (noAnim) spring(stiffness = Spring.StiffnessHigh) else SongSyncMotion.PressSpring,
        label = "pressScale"
    )

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}
