package com.songsync.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.songsync.core.domain.model.Album
import com.songsync.core.domain.model.Artist
import com.songsync.core.domain.model.Folder
import com.songsync.core.domain.model.Genre
import com.songsync.core.domain.model.Playlist
import com.songsync.feature.library.tab.AlbumsTab
import com.songsync.feature.library.tab.ArtistsTab
import com.songsync.feature.library.tab.FoldersTab
import com.songsync.feature.library.tab.GenresTab
import com.songsync.feature.library.tab.PlaylistsTab
import com.songsync.feature.library.tab.SongsTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onArtistClick: (Artist) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onFolderClick: (Folder) -> Unit,
    onGenreClick: (Genre) -> Unit,
    onCreatePlaylist: () -> Unit,
    onRefreshLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }

    val tabTitles = listOf("Songs", "Albums", "Artists", "Genres", "Playlists", "Folders")
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "SongSync",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    // Layout Toggle for Albums and Artists
                    if (uiState.selectedTab == 1) {
                        IconButton(onClick = { viewModel.onEvent(LibraryUiEvent.ToggleAlbumLayout) }) {
                            Icon(
                                imageVector = if (uiState.isAlbumGrid) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                                contentDescription = "Toggle layout"
                            )
                        }
                    } else if (uiState.selectedTab == 2) {
                        IconButton(onClick = { viewModel.onEvent(LibraryUiEvent.ToggleArtistLayout) }) {
                            Icon(
                                imageVector = if (uiState.isArtistGrid) Icons.Rounded.ViewList else Icons.Rounded.GridView,
                                contentDescription = "Toggle layout"
                            )
                        }
                    }

                    // Sort button (active on songs tab)
                    if (uiState.selectedTab == 0) {
                        IconButton(onClick = { showSortSheet = true }) {
                            Icon(imageVector = Icons.Rounded.Sort, contentDescription = "Sort")
                        }
                    }

                    // Search button
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Scrollable Category Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = uiState.selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.selectedTab == index,
                        onClick = { viewModel.onEvent(LibraryUiEvent.SelectTab(index)) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    )
                }
            }

            // Pull-To-Refresh Container
            PullToRefreshBox(
                isRefreshing = uiState.isScanning,
                onRefresh = onRefreshLibrary,
                state = pullToRefreshState,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.selectedTab) {
                        0 -> SongsTab(
                            songs = uiState.songs,
                            selectedIds = uiState.selectedSongIds,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            onSongClick = { song ->
                                viewModel.onEvent(LibraryUiEvent.PlaySong(song, uiState.songs))
                            },
                            onSongLongClick = { song ->
                                viewModel.onEvent(LibraryUiEvent.ToggleSongSelection(song.id))
                            },
                            onToggleFavorite = { song ->
                                viewModel.onEvent(LibraryUiEvent.ToggleFavorite(song.id, !song.isFavorite))
                            }
                        )
                        1 -> AlbumsTab(
                            albums = uiState.albums,
                            isGrid = uiState.isAlbumGrid,
                            onAlbumClick = onAlbumClick
                        )
                        2 -> ArtistsTab(
                            artists = uiState.artists,
                            isGrid = uiState.isArtistGrid,
                            onArtistClick = onArtistClick
                        )
                        3 -> GenresTab(
                            genres = uiState.genres,
                            onGenreClick = onGenreClick
                        )
                        4 -> PlaylistsTab(
                            playlists = uiState.playlists,
                            onPlaylistClick = onPlaylistClick,
                            onCreatePlaylist = onCreatePlaylist,
                            onRenamePlaylist = { /* Handled in dialog */ },
                            onDeletePlaylist = { /* Handled in dialog */ },
                            onDuplicatePlaylist = { /* Handled in dialog */ },
                            onExportM3u = { /* Handled in dialog */ }
                        )
                        5 -> FoldersTab(
                            folders = uiState.folders,
                            onFolderClick = onFolderClick
                        )
                    }

                    // Floating Multi-Select Action Bar
                    AnimatedVisibility(
                        visible = uiState.isMultiSelectMode,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier.align(Alignment.BottomCenter)
                    ) {
                        val selectedSongs = remember(uiState.selectedSongIds, uiState.songs) {
                            uiState.songs.filter { uiState.selectedSongIds.contains(it.id) }
                        }
                        MultiSelectActionBar(
                            selectedCount = uiState.selectedSongIds.size,
                            onClearSelection = { viewModel.onEvent(LibraryUiEvent.ClearSelection) },
                            onPlayNext = {
                                selectedSongs.firstOrNull()?.let {
                                    viewModel.onEvent(LibraryUiEvent.PlayNext(it))
                                }
                            },
                            onAddToQueue = {
                                viewModel.onEvent(LibraryUiEvent.AddSelectedToQueue(selectedSongs))
                            },
                            onAddToPlaylist = { /* Show add-to-playlist dialog */ },
                            onFavorite = {
                                selectedSongs.forEach {
                                    viewModel.onEvent(LibraryUiEvent.ToggleFavorite(it.id, true))
                                }
                            },
                            onShare = { /* Intent share */ },
                            onDelete = {
                                viewModel.onEvent(LibraryUiEvent.DeleteSelectedSongs(uiState.selectedSongIds.toList()))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSortSheet) {
        SortBottomSheet(
            currentSort = uiState.sortOrder,
            onSortSelected = { sortBy, isAsc ->
                viewModel.onEvent(LibraryUiEvent.ChangeSort(sortBy, isAsc))
            },
            onDismiss = { showSortSheet = false }
        )
    }
}
