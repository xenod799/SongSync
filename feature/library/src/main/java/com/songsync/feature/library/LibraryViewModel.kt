package com.songsync.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songsync.core.domain.model.Album
import com.songsync.core.domain.model.Artist
import com.songsync.core.domain.model.Folder
import com.songsync.core.domain.model.Genre
import com.songsync.core.domain.model.Playlist
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.model.SortBy
import com.songsync.core.domain.model.SortOrder
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.domain.repository.SongRepository
import com.songsync.media.player.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibraryUiState(
    val selectedTab: Int = 0,
    val sortOrder: SortOrder = SortOrder(SortBy.TITLE, true),
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val playlists: List<Playlist> = emptyList(),
    val folders: List<Folder> = emptyList(),
    val isAlbumGrid: Boolean = true,
    val isArtistGrid: Boolean = true,
    val isScanning: Boolean = false,
    val scanProgress: Int = 0,
    val selectedSongIds: Set<Long> = emptySet(),
    val isMultiSelectMode: Boolean = false
)

sealed interface LibraryUiEvent {
    data class SelectTab(val tabIndex: Int) : LibraryUiEvent
    data class ChangeSort(val sortBy: SortBy, val isAscending: Boolean) : LibraryUiEvent
    data class ToggleSongSelection(val songId: Long) : LibraryUiEvent
    data object ClearSelection : LibraryUiEvent
    data object SelectAll : LibraryUiEvent
    data class PlaySong(val song: Song, val queue: List<Song>) : LibraryUiEvent
    data class ToggleFavorite(val songId: Long, val isFavorite: Boolean) : LibraryUiEvent
    data class DeleteSelectedSongs(val songIds: List<Long>) : LibraryUiEvent
    data class AddSelectedToQueue(val songs: List<Song>) : LibraryUiEvent
    data class PlayNext(val song: Song) : LibraryUiEvent
    data object ToggleAlbumLayout : LibraryUiEvent
    data object ToggleArtistLayout : LibraryUiEvent
}

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository,
    val playerConnection: PlayerConnection
) : ViewModel() {

    private val _sortOrder = MutableStateFlow(SortOrder(SortBy.TITLE, true))
    private val _selectedTab = MutableStateFlow(0)
    private val _selectedSongIds = MutableStateFlow<Set<Long>>(emptySet())
    private val _isScanning = MutableStateFlow(false)
    private val _scanProgress = MutableStateFlow(0)

    val uiState: StateFlow<LibraryUiState> = combine(
        _selectedTab,
        _sortOrder,
        _sortOrder.flatMapLatest { songRepository.getSongs(it) },
        songRepository.getAlbums(),
        songRepository.getArtists(),
        songRepository.getGenres(),
        playlistRepository.getPlaylists(),
        songRepository.getFolders(),
        _selectedSongIds,
        settingsRepository.settingsFlow,
        _isScanning,
        _scanProgress
    ) { params ->
        val tab = params[0] as Int
        val sort = params[1] as SortOrder
        val songs = params[2] as List<Song>
        val albums = params[3] as List<Album>
        val artists = params[4] as List<Artist>
        val genres = params[5] as List<Genre>
        val playlists = params[6] as List<Playlist>
        val folders = params[7] as List<Folder>
        val selectedIds = params[8] as Set<Long>
        val settings = params[9] as com.songsync.core.domain.model.UserSettings
        val scanning = params[10] as Boolean
        val progress = params[11] as Int

        LibraryUiState(
            selectedTab = tab,
            sortOrder = sort,
            songs = songs,
            albums = albums,
            artists = artists,
            genres = genres,
            playlists = playlists,
            folders = folders,
            isAlbumGrid = settings.isAlbumGrid,
            isArtistGrid = settings.isArtistGrid,
            selectedSongIds = selectedIds,
            isMultiSelectMode = selectedIds.isNotEmpty(),
            isScanning = scanning,
            scanProgress = progress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LibraryUiState()
    )

    fun onEvent(event: LibraryUiEvent) {
        when (event) {
            is LibraryUiEvent.SelectTab -> _selectedTab.value = event.tabIndex
            is LibraryUiEvent.ChangeSort -> _sortOrder.value = SortOrder(event.sortBy, event.isAscending)
            is LibraryUiEvent.ToggleSongSelection -> {
                _selectedSongIds.update { set ->
                    if (set.contains(event.songId)) set - event.songId else set + event.songId
                }
            }
            is LibraryUiEvent.ClearSelection -> _selectedSongIds.value = emptySet()
            is LibraryUiEvent.SelectAll -> {
                _selectedSongIds.value = uiState.value.songs.map { it.id }.toSet()
            }
            is LibraryUiEvent.PlaySong -> {
                playerConnection.playSong(event.song, event.queue)
            }
            is LibraryUiEvent.ToggleFavorite -> {
                viewModelScope.launch {
                    songRepository.toggleFavorite(event.songId, event.isFavorite)
                }
            }
            is LibraryUiEvent.DeleteSelectedSongs -> {
                viewModelScope.launch {
                    songRepository.deleteSongs(event.songIds)
                    _selectedSongIds.value = emptySet()
                }
            }
            is LibraryUiEvent.AddSelectedToQueue -> {
                event.songs.forEach { playerConnection.addToQueue(it) }
                _selectedSongIds.value = emptySet()
            }
            is LibraryUiEvent.PlayNext -> {
                playerConnection.playNext(event.song)
            }
            is LibraryUiEvent.ToggleAlbumLayout -> {
                viewModelScope.launch {
                    settingsRepository.setAlbumGrid(!uiState.value.isAlbumGrid)
                }
            }
            is LibraryUiEvent.ToggleArtistLayout -> {
                viewModelScope.launch {
                    settingsRepository.setArtistGrid(!uiState.value.isArtistGrid)
                }
            }
        }
    }

    fun setScanningProgress(scanning: Boolean, progress: Int) {
        _isScanning.value = scanning
        _scanProgress.value = progress
    }
}
