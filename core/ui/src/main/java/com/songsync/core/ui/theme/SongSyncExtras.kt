package com.songsync.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class SongSyncExtras(
    val butter: Color,
    val butterContainer: Color,
    val blush: Color
)

val SongSyncExtrasLight = SongSyncExtras(
    butter = Color(0xFFFFE08A),
    butterContainer = Color(0xFFFFF0C2),
    blush = Color(0xFFFFC2D1)
)

val SongSyncExtrasDark = SongSyncExtras(
    butter = Color(0xFFFFD866),
    butterContainer = Color(0xFF4D3F00),
    blush = Color(0xFF8A2A4B)
)

val LocalSongSyncExtras = staticCompositionLocalOf { SongSyncExtrasLight }
