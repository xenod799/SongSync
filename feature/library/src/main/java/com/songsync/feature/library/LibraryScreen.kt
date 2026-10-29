package com.songsync.feature.library

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.ViewList
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.songsync.core.domain.model.Album
import com.songsync.core.domain.model.Artist
import com.songsync.core.domain.model.Folder
import com.songsync.core.domain.model.Genre
import com.songsync.core.domain.model.Playlist
import com.songsync.core.domain.model.Song
import com.songsync.core.ui.theme.PillShape
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
    onNavigateToSearch: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSongClick: (Song, List<Song>) -> Unit = { song, queue -> viewModel.onEvent(LibraryUiEvent.PlaySong(song, queue)) },
    onAlbumClick: (Album) -> Unit = {},
    onArtistClick: (Artist) -> Unit = {},
    onPlaylistClick: (Playlist) -> Unit = {},
    onFolderClick: (Folder) -> Unit = {},
    onGenreClick: (Genre) -> Unit = {},
    onCreatePlaylist: (() -> Unit)? = null,
    onRefreshLibrary: () -> Unit = { viewModel.refreshLibrary() },
    onEditTags: ((Song) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSortSheet by remember { mutableStateOf(false) }

    // Playlist dialog states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var playlistToRename by remember { mutableStateOf<Playlist?>(null) }
    var playlistToDelete by remember { mutableStateOf<Playlist?>(null) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    // Storage permission state & launcher
    val context = LocalContext.current
    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasStoragePermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[audioPermission] == true ||
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
        hasStoragePermission = granted
        if (granted) {
            viewModel.refreshLibrary()
        }
    }

    val requestPermissionAction = {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(perms)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        hasStoragePermission =
            ContextCompat.checkSelfPermission(context, audioPermission) == PackageManager.PERMISSION_GRANTED
    }

    var permissionRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(hasStoragePermission) {
        if (!hasStoragePermission && !permissionRequested) {
            permissionRequested = true
            requestPermissionAction()
        }
    }

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
            // Permission Banner (if not granted)
            if (!hasStoragePermission) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FolderOpen,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Storage Permission Needed",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Allow storage access so SongSync can discover your offline music files.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = requestPermissionAction,
                            shape = PillShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("Grant", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

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
                                onSongClick(song, uiState.songs)
                            },
                            onSongLongClick = { song ->
                                viewModel.onEvent(LibraryUiEvent.ToggleSongSelection(song.id))
                            },
                            onToggleFavorite = { song ->
                                viewModel.onEvent(LibraryUiEvent.ToggleFavorite(song.id, !song.isFavorite))
                            },
                            onEditTags = onEditTags,
                            hasPermission = hasStoragePermission,
                            onRequestPermission = requestPermissionAction,
                            onScanClick = onRefreshLibrary
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
                            onCreatePlaylist = {
                                if (onCreatePlaylist != null) {
                                    onCreatePlaylist()
                                } else {
                                    showCreatePlaylistDialog = true
                                }
                            },
                            onRenamePlaylist = { playlistToRename = it },
                            onDeletePlaylist = { playlistToDelete = it },
                            onDuplicatePlaylist = { viewModel.duplicatePlaylist(it.id, "${it.name} (Copy)") },
                            onExportM3u = { viewModel.exportM3u(it.id) }
                        )
                        5 -> FoldersTab(
                            folders = uiState.folders,
                            onFolderClick = onFolderClick
                        )
                    }

                    // Floating Multi-Select Action Bar
                    androidx.compose.animation.AnimatedVisibility(
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
                            onAddToPlaylist = { showAddToPlaylistDialog = true },
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

    // Sort Bottom Sheet
    if (showSortSheet) {
        SortBottomSheet(
            currentSort = uiState.sortOrder,
            onSortSelected = { sortBy, isAsc ->
                viewModel.onEvent(LibraryUiEvent.ChangeSort(sortBy, isAsc))
            },
            onDismiss = { showSortSheet = false }
        )
    }

    // Create Playlist Dialog
    if (showCreatePlaylistDialog) {
        var playlistName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            title = { Text("Create Playlist") },
            text = {
                Column {
                    Text(
                        text = "Enter a name for your new offline playlist:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = playlistName,
                        onValueChange = { playlistName = it },
                        placeholder = { Text("Playlist name") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = playlistName.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.createPlaylist(trimmed)
                            showCreatePlaylistDialog = false
                        }
                    },
                    shape = PillShape,
                    enabled = playlistName.isNotBlank()
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename Playlist Dialog
    playlistToRename?.let { playlist ->
        var newName by remember { mutableStateOf(playlist.name) }
        AlertDialog(
            onDismissRequest = { playlistToRename = null },
            title = { Text("Rename Playlist") },
            text = {
                Column {
                    Text(
                        text = "Enter a new name for '${playlist.name}':",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newName.trim()
                        if (trimmed.isNotBlank()) {
                            viewModel.renamePlaylist(playlist.id, trimmed)
                            playlistToRename = null
                        }
                    },
                    shape = PillShape,
                    enabled = newName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToRename = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Playlist Dialog
    playlistToDelete?.let { playlist ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = { Text("Delete Playlist") },
            text = {
                Text("Are you sure you want to delete '${playlist.name}'? Your audio files will not be deleted.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePlaylist(playlist.id)
                        playlistToDelete = null
                    },
                    shape = PillShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add to Playlist Dialog (Multi-Select)
    if (showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showAddToPlaylistDialog = false },
            title = { Text("Add to Playlist") },
            text = {
                if (uiState.playlists.isEmpty()) {
                    Text("No playlists yet. Create a playlist first from the Playlists tab.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(uiState.playlists, key = { it.id }) { pl ->
                            ListItem(
                                headlineContent = { Text(pl.name) },
                                supportingContent = { Text("${pl.trackCount} songs") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSongsToPlaylist(pl.id, uiState.selectedSongIds.toList())
                                        viewModel.onEvent(LibraryUiEvent.ClearSelection)
                                        showAddToPlaylistDialog = false
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddToPlaylistDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
