package com.songsync.feature.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import androidx.media3.common.PlaybackParameters
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.Lyrics
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.LyricsRepository
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.domain.repository.SongRepository
import com.songsync.media.player.PlaybackState
import com.songsync.media.player.SleepTimerManager
import com.songsync.media.player.PlayerConnection
import com.songsync.media.player.EqualizerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class PlayerUiState(
    val playbackState: PlaybackState = PlaybackState(),
    val artworkColorHex: Int? = null,
    val artShape: ArtShape = ArtShape.ROUNDED_SQUARE,
    val animateArtPulse: Boolean = true,
    val lyrics: Lyrics? = null,
    val showQueueSheet: Boolean = false,
    val showLyricsSheet: Boolean = false,
    val showSongInfoSheet: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val playerConnection: PlayerConnection,
    val sleepTimerManager: SleepTimerManager,
    val equalizerManager: EqualizerManager,
    private val songRepository: SongRepository,
    private val lyricsRepository: LyricsRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val playbackState: StateFlow<PlaybackState> = playerConnection.playbackState

    private val _artworkColor = MutableStateFlow<Int?>(null)
    val artworkColor: StateFlow<Int?> = _artworkColor.asStateFlow()

    private val _currentLyrics = MutableStateFlow<Lyrics?>(null)
    val currentLyrics: StateFlow<Lyrics?> = _currentLyrics.asStateFlow()

    val playbackParameters: StateFlow<PlaybackParameters> = playerConnection.playbackState.map {
        PlaybackParameters(it.playbackSpeed, it.playbackPitch)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaybackParameters(1f, 1f)
    )

    val settings = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = com.songsync.core.domain.model.UserSettings()
    )

    init {
        observeCurrentSong()
    }

    private fun observeCurrentSong() {
        viewModelScope.launch {
            playerConnection.playbackState.collectLatest { state ->
                val song = state.currentSong
                if (song != null) {
                    extractArtworkColor(song.albumArtUri)
                    loadLyrics(song.id, song.dataPath)
                } else {
                    _artworkColor.value = null
                    _currentLyrics.value = null
                }
            }
        }
    }

    private suspend fun extractArtworkColor(artUri: Uri?) = withContext(Dispatchers.IO) {
        if (artUri == null) {
            _artworkColor.value = null
            return@withContext
        }
        try {
            val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, artUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.setTargetSize(120, 120)
                }
            } else {
                MediaStore.Images.Media.getBitmap(context.contentResolver, artUri)
            }

            bitmap?.let {
                val palette = Palette.from(it).generate()
                val dominant = palette.getVibrantColor(palette.getDominantColor(0))
                if (dominant != 0) {
                    _artworkColor.value = dominant
                }
            }
        } catch (_: Exception) {
            _artworkColor.value = null
        }
    }

    private fun loadLyrics(songId: Long, path: String) {
        viewModelScope.launch {
            lyricsRepository.getLyrics(songId, path).collectLatest {
                _currentLyrics.value = it
            }
        }
    }

    fun adjustLyricsOffset(offsetDeltaMs: Long) {
        val lyrics = _currentLyrics.value ?: return
        val newOffset = lyrics.offsetMs + offsetDeltaMs
        viewModelScope.launch {
            lyricsRepository.saveLyricsOffset(lyrics.songId, newOffset)
            _currentLyrics.value = lyrics.copy(offsetMs = newOffset)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.toggleFavorite(song.id, !song.isFavorite)
        }
    }

    fun saveQueueAsPlaylist(name: String) {
        val queue = playerConnection.playbackState.value.queue
        if (queue.isEmpty() || name.isBlank()) return
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(name.trim())
            playlistRepository.addSongsToPlaylist(playlistId, queue.map { it.id })
        }
    }

    fun playSong(song: Song, queue: List<Song>) {
        playerConnection.playSong(song, queue)
    }

    fun togglePlayPause() {
        playerConnection.togglePlayPause()
    }

    fun skipToNext() {
        playerConnection.skipToNext()
    }

    fun skipToPrevious() {
        playerConnection.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        playerConnection.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerConnection.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerConnection.cycleRepeatMode()
    }

    fun setPlaybackParameters(speed: Float, pitch: Float) {
        playerConnection.setPlaybackParameters(speed, pitch)
    }

    fun playUri(uri: android.net.Uri) {
        playerConnection.playUri(uri)
    }
}
