package com.songsync.feature.library.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SongRepository
import com.songsync.core.ui.component.ExpressiveMusicNotePlaceholder
import com.songsync.feature.library.tab.SongListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Detail entity type keys used by [LibraryDetailRoute] navigation arguments. */
object DetailType {
    const val ALBUM = "album"
    const val ARTIST = "artist"
    const val GENRE = "genre"
    const val FOLDER = "folder"
    const val PLAYLIST = "playlist"
}

@HiltViewModel
class SongListDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val playerConnection: com.songsync.media.player.PlayerConnection
) : ViewModel() {

    private val type: String = savedStateHandle.get<String>("type") ?: DetailType.ALBUM
    private val key: String = savedStateHandle.get<String>("key") ?: ""
    val title: String = savedStateHandle.get<String>("title") ?: ""

    val songs: StateFlow<List<Song>> = when (type) {
        DetailType.ALBUM -> songRepository.getAlbumSongs(key.toLongOrNull() ?: -1L)
        DetailType.ARTIST -> songRepository.getArtistSongs(key.toLongOrNull() ?: -1L)
        DetailType.GENRE -> songRepository.getGenreSongs(key)
        DetailType.FOLDER -> songRepository.getFolderSongs(key)
        DetailType.PLAYLIST -> playlistRepository.getPlaylistSongs(key.toLongOrNull() ?: -1L)
        else -> flowOf(emptyList())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun playSong(song: Song, queue: List<Song>) {
        playerConnection.playSong(song, queue)
    }

    fun playAll() {
        val list = songs.value
        val first = list.firstOrNull() ?: return
        playSong(first, list)
    }

    fun shuffleAll() {
        val list = songs.value
        if (list.isEmpty()) return
        playerConnection.setShuffleMode(true)
        playSong(list.random(), list)
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            songRepository.toggleFavorite(song.id, !song.isFavorite)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListDetailScreen(
    viewModel: SongListDetailViewModel,
    onNavigateBack: () -> Unit,
    onSongClick: (Song, List<Song>) -> Unit,
    modifier: Modifier = Modifier
) {
    val songs by viewModel.songs.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = viewModel.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val count = songs.size
                    Text(
                        text = "$count ${if (count == 1) "song" else "songs"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Navigate back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (songs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ExpressiveMusicNotePlaceholder(modifier = Modifier.size(120.dp))
                    Text(
                        text = "Nothing here yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.playAll() }) {
                        Icon(
                            imageVector = Icons.Rounded.PlayCircle,
                            contentDescription = "Play all",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text("Play all", style = MaterialTheme.typography.titleSmall)
                    IconButton(onClick = { viewModel.shuffleAll() }) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = "Shuffle",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text("Shuffle", style = MaterialTheme.typography.titleSmall)
                }
            }

            items(songs, key = { it.id }) { song ->
                SongListItem(
                    song = song,
                    isSelected = false,
                    isMultiSelectMode = false,
                    onClick = { onSongClick(song, songs) },
                    onLongClick = { /* no multi-select in detail view */ },
                    onToggleFavorite = { viewModel.toggleFavorite(song) }
                )
            }
        }
    }
}
