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
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.Lyrics
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.LyricsRepository
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.domain.repository.SongRepository
import com.songsync.media.player.PlaybackState
import com.songsync.media.player.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
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
    private val songRepository: SongRepository,
    private val lyricsRepository: LyricsRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _artworkColor = MutableStateFlow<Int?>(null)
    val artworkColor: StateFlow<Int?> = _artworkColor.asStateFlow()

    private val _currentLyrics = MutableStateFlow<Lyrics?>(null)
    val currentLyrics: StateFlow<Lyrics?> = _currentLyrics.asStateFlow()

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
}
