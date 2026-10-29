package com.songsync.media.player

import kotlin.math.pow

object ReplayGainHelper {
    /**
     * Calculates volume multiplier for ExoPlayer based on ReplayGain track gain in dB.
     * Prevents clipping by checking track peak if available.
     */
    fun calculateVolume(gainDb: Float?, peak: Float?, isEnabled: Boolean): Float {
        if (!isEnabled || gainDb == null) return 1.0f

        // Convert dB gain to linear scale: 10^(dB/20)
        var linearScale = 10.0.pow(gainDb.toDouble() / 20.0).toFloat()

        // Clip prevention: if linearScale * peak > 1.0, scale back to avoid distortion
        if (peak != null && peak > 0f) {
            if (linearScale * peak > 1.0f) {
                linearScale = 1.0f / peak
            }
        }

        return linearScale.coerceIn(0.1f, 2.0f)
    }
}
