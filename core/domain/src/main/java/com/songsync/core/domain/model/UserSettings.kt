package com.songsync.core.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    AMOLED
}

enum class ColorPalette {
    PEACH_POP,
    MINT_SODA,
    LAVENDER_DREAM,
    BUBBLEGUM,
    SUNNY_DAY
}

enum class ArtShape {
    ROUNDED_SQUARE,
    CIRCLE,
    COOKIE,
    SOFT_BURST
}

data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ColorPalette = ColorPalette.PEACH_POP,
    val useDynamicColor: Boolean = false,
    val artShape: ArtShape = ArtShape.ROUNDED_SQUARE,
    val animateArtPulse: Boolean = true,
    val isAlbumGrid: Boolean = true,
    val isArtistGrid: Boolean = true,
    val minTrackDurationSeconds: Int = 30,
    val excludedFolders: Set<String> = emptySet(),
    val crossfadeDurationSeconds: Int = 0,
    val isGaplessEnabled: Boolean = true,
    val isReplayGainEnabled: Boolean = true,
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val lyricsTextSizeSp: Int = 18,
    val lyricsDefaultOffsetMs: Long = 0L,
    val onboardingCompleted: Boolean = false
)
