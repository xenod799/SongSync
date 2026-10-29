package com.songsync.core.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.ColorPalette
import com.songsync.core.domain.model.ThemeMode
import com.songsync.core.domain.model.UserSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "songsync_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PALETTE = stringPreferencesKey("palette")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val ART_SHAPE = stringPreferencesKey("art_shape")
        val ANIMATE_ART_PULSE = booleanPreferencesKey("animate_art_pulse")
        val ALBUM_GRID = booleanPreferencesKey("album_grid")
        val ARTIST_GRID = booleanPreferencesKey("artist_grid")
        val MIN_DURATION_SECONDS = intPreferencesKey("min_duration_seconds")
        val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
        val CROSSFADE_SECONDS = intPreferencesKey("crossfade_seconds")
        val GAPLESS_ENABLED = booleanPreferencesKey("gapless_enabled")
        val REPLAYGAIN_ENABLED = booleanPreferencesKey("replaygain_enabled")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val PLAYBACK_PITCH = floatPreferencesKey("playback_pitch")
        val LYRICS_TEXT_SIZE_SP = intPreferencesKey("lyrics_text_size_sp")
        val LYRICS_DEFAULT_OFFSET_MS = longPreferencesKey("lyrics_default_offset_ms")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name),
            palette = ColorPalette.valueOf(prefs[Keys.PALETTE] ?: ColorPalette.PEACH_POP.name),
            useDynamicColor = prefs[Keys.DYNAMIC_COLOR] ?: false,
            artShape = ArtShape.valueOf(prefs[Keys.ART_SHAPE] ?: ArtShape.ROUNDED_SQUARE.name),
            animateArtPulse = prefs[Keys.ANIMATE_ART_PULSE] ?: true,
            isAlbumGrid = prefs[Keys.ALBUM_GRID] ?: true,
            isArtistGrid = prefs[Keys.ARTIST_GRID] ?: true,
            minTrackDurationSeconds = prefs[Keys.MIN_DURATION_SECONDS] ?: 30,
            excludedFolders = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet(),
            crossfadeDurationSeconds = prefs[Keys.CROSSFADE_SECONDS] ?: 0,
            isGaplessEnabled = prefs[Keys.GAPLESS_ENABLED] ?: true,
            isReplayGainEnabled = prefs[Keys.REPLAYGAIN_ENABLED] ?: true,
            playbackSpeed = prefs[Keys.PLAYBACK_SPEED] ?: 1.0f,
            playbackPitch = prefs[Keys.PLAYBACK_PITCH] ?: 1.0f,
            lyricsTextSizeSp = prefs[Keys.LYRICS_TEXT_SIZE_SP] ?: 18,
            lyricsDefaultOffsetMs = prefs[Keys.LYRICS_DEFAULT_OFFSET_MS] ?: 0L,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setPalette(palette: ColorPalette) {
        context.dataStore.edit { it[Keys.PALETTE] = palette.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setArtShape(shape: ArtShape) {
        context.dataStore.edit { it[Keys.ART_SHAPE] = shape.name }
    }

    suspend fun setAnimateArtPulse(animate: Boolean) {
        context.dataStore.edit { it[Keys.ANIMATE_ART_PULSE] = animate }
    }

    suspend fun setAlbumGrid(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.ALBUM_GRID] = isGrid }
    }

    suspend fun setArtistGrid(isGrid: Boolean) {
        context.dataStore.edit { it[Keys.ARTIST_GRID] = isGrid }
    }

    suspend fun setMinDurationSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.MIN_DURATION_SECONDS] = seconds }
    }

    suspend fun setExcludedFolders(folders: Set<String>) {
        context.dataStore.edit { it[Keys.EXCLUDED_FOLDERS] = folders }
    }

    suspend fun setCrossfadeDurationSeconds(seconds: Int) {
        context.dataStore.edit { it[Keys.CROSSFADE_SECONDS] = seconds }
    }

    suspend fun setGaplessEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.GAPLESS_ENABLED] = enabled }
    }

    suspend fun setReplayGainEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REPLAYGAIN_ENABLED] = enabled }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_SPEED] = speed }
    }

    suspend fun setPlaybackPitch(pitch: Float) {
        context.dataStore.edit { it[Keys.PLAYBACK_PITCH] = pitch }
    }

    suspend fun setLyricsTextSizeSp(sizeSp: Int) {
        context.dataStore.edit { it[Keys.LYRICS_TEXT_SIZE_SP] = sizeSp }
    }

    suspend fun setLyricsDefaultOffsetMs(offsetMs: Long) {
        context.dataStore.edit { it[Keys.LYRICS_DEFAULT_OFFSET_MS] = offsetMs }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }
}
