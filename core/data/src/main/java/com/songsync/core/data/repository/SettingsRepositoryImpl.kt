package com.songsync.core.data.repository

import com.songsync.core.data.local.datastore.SettingsDataStore
import com.songsync.core.data.local.db.dao.PlaylistDao
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.local.db.entity.PlaylistEntity
import com.songsync.core.data.local.db.entity.PlaylistSongCrossRef
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.ColorPalette
import com.songsync.core.domain.model.ThemeMode
import com.songsync.core.domain.model.UserSettings
import com.songsync.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class BackupData(
    val themeMode: String,
    val palette: String,
    val useDynamicColor: Boolean,
    val artShape: String,
    val animateArtPulse: Boolean,
    val isAlbumGrid: Boolean,
    val isArtistGrid: Boolean,
    val minTrackDurationSeconds: Int,
    val excludedFolders: List<String>,
    val crossfadeDurationSeconds: Int,
    val isGaplessEnabled: Boolean,
    val isReplayGainEnabled: Boolean,
    val playbackSpeed: Float,
    val playbackPitch: Float,
    val lyricsTextSizeSp: Int,
    val lyricsDefaultOffsetMs: Long,
    val favoriteSongMediaIds: List<Long>,
    val playlists: List<BackupPlaylist>
)

@Serializable
data class BackupPlaylist(
    val name: String,
    val songMediaIds: List<Long>
)

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) : SettingsRepository {

    override val settingsFlow: Flow<UserSettings> = dataStore.settingsFlow

    override suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)
    override suspend fun setColorPalette(palette: ColorPalette) = dataStore.setPalette(palette)
    override suspend fun setUseDynamicColor(useDynamic: Boolean) = dataStore.setDynamicColor(useDynamic)
    override suspend fun setArtShape(shape: ArtShape) = dataStore.setArtShape(shape)
    override suspend fun setAnimateArtPulse(animate: Boolean) = dataStore.setAnimateArtPulse(animate)
    override suspend fun setAlbumGrid(isGrid: Boolean) = dataStore.setAlbumGrid(isGrid)
    override suspend fun setArtistGrid(isGrid: Boolean) = dataStore.setArtistGrid(isGrid)
    override suspend fun setMinTrackDurationSeconds(seconds: Int) = dataStore.setMinDurationSeconds(seconds)
    override suspend fun setExcludedFolders(folders: Set<String>) = dataStore.setExcludedFolders(folders)
    override suspend fun setCrossfadeDurationSeconds(seconds: Int) = dataStore.setCrossfadeDurationSeconds(seconds)
    override suspend fun setGaplessEnabled(enabled: Boolean) = dataStore.setGaplessEnabled(enabled)
    override suspend fun setReplayGainEnabled(enabled: Boolean) = dataStore.setReplayGainEnabled(enabled)
    override suspend fun setPlaybackSpeed(speed: Float) = dataStore.setPlaybackSpeed(speed)
    override suspend fun setPlaybackPitch(pitch: Float) = dataStore.setPlaybackPitch(pitch)
    override suspend fun setLyricsTextSizeSp(sizeSp: Int) = dataStore.setLyricsTextSizeSp(sizeSp)
    override suspend fun setLyricsDefaultOffsetMs(offsetMs: Long) = dataStore.setLyricsDefaultOffsetMs(offsetMs)
    override suspend fun setOnboardingCompleted(completed: Boolean) = dataStore.setOnboardingCompleted(completed)

    override suspend fun exportBackupJson(): String {
        val currentSettings = settingsFlow.first()
        val favorites = songDao.getFavoriteSongs().first()
        val allPlaylists = playlistDao.getAllPlaylists()
        val backupPlaylists = allPlaylists.map { playlist ->
            val songs = playlistDao.getSongsForPlaylistSync(playlist.id)
            BackupPlaylist(
                name = playlist.name,
                songMediaIds = songs.map { it.mediaStoreId }
            )
        }

        val backup = BackupData(
            themeMode = currentSettings.themeMode.name,
            palette = currentSettings.palette.name,
            useDynamicColor = currentSettings.useDynamicColor,
            artShape = currentSettings.artShape.name,
            animateArtPulse = currentSettings.animateArtPulse,
            isAlbumGrid = currentSettings.isAlbumGrid,
            isArtistGrid = currentSettings.isArtistGrid,
            minTrackDurationSeconds = currentSettings.minTrackDurationSeconds,
            excludedFolders = currentSettings.excludedFolders.toList(),
            crossfadeDurationSeconds = currentSettings.crossfadeDurationSeconds,
            isGaplessEnabled = currentSettings.isGaplessEnabled,
            isReplayGainEnabled = currentSettings.isReplayGainEnabled,
            playbackSpeed = currentSettings.playbackSpeed,
            playbackPitch = currentSettings.playbackPitch,
            lyricsTextSizeSp = currentSettings.lyricsTextSizeSp,
            lyricsDefaultOffsetMs = currentSettings.lyricsDefaultOffsetMs,
            favoriteSongMediaIds = favorites.map { it.mediaStoreId },
            playlists = backupPlaylists
        )

        val json = Json { prettyPrint = true }
        return json.encodeToString(backup)
    }

    override suspend fun restoreBackupJson(jsonString: String): Boolean {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val backup = json.decodeFromString<BackupData>(jsonString)

            setThemeMode(ThemeMode.valueOf(backup.themeMode))
            setColorPalette(ColorPalette.valueOf(backup.palette))
            setUseDynamicColor(backup.useDynamicColor)
            setArtShape(ArtShape.valueOf(backup.artShape))
            setAnimateArtPulse(backup.animateArtPulse)
            setAlbumGrid(backup.isAlbumGrid)
            setArtistGrid(backup.isArtistGrid)
            setMinTrackDurationSeconds(backup.minTrackDurationSeconds)
            setExcludedFolders(backup.excludedFolders.toSet())
            setCrossfadeDurationSeconds(backup.crossfadeDurationSeconds)
            setGaplessEnabled(backup.isGaplessEnabled)
            setReplayGainEnabled(backup.isReplayGainEnabled)
            setPlaybackSpeed(backup.playbackSpeed)
            setPlaybackPitch(backup.playbackPitch)
            setLyricsTextSizeSp(backup.lyricsTextSizeSp)
            setLyricsDefaultOffsetMs(backup.lyricsDefaultOffsetMs)

            // Restore favorites
            val allSongs = songDao.getAllSongsList()
            val mediaStoreIdMap = allSongs.associateBy { it.mediaStoreId }
            backup.favoriteSongMediaIds.forEach { mediaId ->
                mediaStoreIdMap[mediaId]?.let { song ->
                    songDao.updateFavorite(song.id, true)
                }
            }

            // Restore playlists
            backup.playlists.forEach { bp ->
                val playlistId = playlistDao.insertPlaylist(PlaylistEntity(name = bp.name))
                val songIds = bp.songMediaIds.mapNotNull { mediaStoreIdMap[it]?.id }
                if (songIds.isNotEmpty()) {
                    playlistDao.addSongsToPlaylist(playlistId, songIds)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
