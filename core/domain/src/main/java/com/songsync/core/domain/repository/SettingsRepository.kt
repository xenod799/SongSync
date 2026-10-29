package com.songsync.core.domain.repository

import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.ColorPalette
import com.songsync.core.domain.model.ThemeMode
import com.songsync.core.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settingsFlow: Flow<UserSettings>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setColorPalette(palette: ColorPalette)
    suspend fun setUseDynamicColor(useDynamic: Boolean)
    suspend fun setArtShape(shape: ArtShape)
    suspend fun setAnimateArtPulse(animate: Boolean)
    suspend fun setAlbumGrid(isGrid: Boolean)
    suspend fun setArtistGrid(isGrid: Boolean)
    suspend fun setMinTrackDurationSeconds(seconds: Int)
    suspend fun setExcludedFolders(folders: Set<String>)
    suspend fun setCrossfadeDurationSeconds(seconds: Int)
    suspend fun setGaplessEnabled(enabled: Boolean)
    suspend fun setReplayGainEnabled(enabled: Boolean)
    suspend fun setPlaybackSpeed(speed: Float)
    suspend fun setPlaybackPitch(pitch: Float)
    suspend fun setLyricsTextSizeSp(sizeSp: Int)
    suspend fun setLyricsDefaultOffsetMs(offsetMs: Long)
    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun exportBackupJson(): String
    suspend fun restoreBackupJson(jsonString: String): Boolean
}
