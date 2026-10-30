package com.songsync.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.songsync.core.domain.model.Song
import com.songsync.core.ui.component.FloatingNavBar
import com.songsync.core.ui.component.NavItem
import com.songsync.feature.library.LibraryScreen
import com.songsync.feature.library.LibraryViewModel
import com.songsync.feature.library.detail.DetailType
import com.songsync.feature.library.detail.SongListDetailScreen
import com.songsync.feature.library.search.SearchScreen
import com.songsync.feature.library.search.SearchViewModel
import com.songsync.feature.library.tageditor.TagEditorBottomSheet
import com.songsync.feature.player.EqualizerScreen
import com.songsync.feature.player.LyricsBottomSheet
import com.songsync.feature.player.MiniPlayer
import com.songsync.feature.player.NowPlayingScreen
import com.songsync.feature.player.PlayerViewModel
import com.songsync.feature.player.QueueBottomSheet
import com.songsync.feature.player.SleepTimerBottomSheet
import com.songsync.feature.player.SongInfoBottomSheet
import com.songsync.feature.player.SpeedPitchBottomSheet
import com.songsync.feature.settings.OnboardingScreen
import com.songsync.feature.settings.SettingsScreen
import com.songsync.feature.settings.SettingsViewModel
import com.songsync.media.sync.MediaSyncWorker
import kotlinx.serialization.Serializable

@Serializable
object LibraryRoute

@Serializable
object SearchRoute

@Serializable
object SettingsRoute

@Serializable
object EqualizerRoute

@Serializable
object OnboardingRoute

@Serializable
data class LibraryDetailRoute(
    val type: String,
    val key: String,
    val title: String
)

@Composable
fun SongSyncNavGraph(
    isOnboardingCompleted: Boolean,
    onOnboardingCompleted: () -> Unit,
    modifier: Modifier = Modifier,
    playerViewModel: PlayerViewModel = hiltViewModel(),
    libraryViewModel: LibraryViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playbackState by playerViewModel.playbackState.collectAsState()
    val currentLyrics by playerViewModel.currentLyrics.collectAsState()

    var showNowPlaying by remember { mutableStateOf(false) }
    var showQueue by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showSongInfo by remember { mutableStateOf(false) }
    var showSleepTimer by remember { mutableStateOf(false) }
    var showSpeedPitch by remember { mutableStateOf(false) }
    var tagEditorSong by remember { mutableStateOf<Song?>(null) }

    val startDestination = if (isOnboardingCompleted) LibraryRoute else OnboardingRoute

    val navItems = listOf(
        NavItem(
            title = "Library",
            iconOutlined = Icons.Outlined.LibraryMusic,
            iconFilled = Icons.Rounded.LibraryMusic
        ),
        NavItem(
            title = "Search",
            iconOutlined = Icons.Outlined.Search,
            iconFilled = Icons.Rounded.Search
        ),
        NavItem(
            title = "Settings",
            iconOutlined = Icons.Outlined.Settings,
            iconFilled = Icons.Rounded.Settings
        )
    )

    val selectedNavIndex = when (currentRoute) {
        SearchRoute::class.qualifiedName -> 1
        SettingsRoute::class.qualifiedName -> 2
        else -> 0
    }

    val isTopLevelRoute = currentRoute == LibraryRoute::class.qualifiedName ||
            currentRoute == SearchRoute::class.qualifiedName ||
            currentRoute == SettingsRoute::class.qualifiedName

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isOnboardingCompleted && isTopLevelRoute) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(bottom = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Mini Player
                        if (playbackState.currentSong != null && !showNowPlaying) {
                            MiniPlayer(
                                playbackState = playbackState,
                                onExpandNowPlaying = { showNowPlaying = true },
                                onTogglePlayPause = { playerViewModel.togglePlayPause() },
                                onNext = { playerViewModel.skipToNext() }
                            )
                        }

                        // Floating Navigation Bar
                        FloatingNavBar(
                            items = navItems,
                            selectedIndex = selectedNavIndex,
                            onItemSelected = { index ->
                                when (index) {
                                    0 -> navController.navigate(LibraryRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    1 -> navController.navigate(SearchRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                    2 -> navController.navigate(SettingsRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
            ) {
                composable<OnboardingRoute> {
                    OnboardingScreen(
                        onOnboardingFinished = {
                            onOnboardingCompleted()
                            MediaSyncWorker.enqueueOneTimeSync(context)
                            navController.navigate(LibraryRoute) {
                                popUpTo(OnboardingRoute) { inclusive = true }
                            }
                        }
                    )
                }

                composable<LibraryRoute> {
                    LibraryScreen(
                        viewModel = libraryViewModel,
                        onSongClick = { song, queue ->
                            playerViewModel.playSong(song, queue)
                        },
                        onNavigateToSearch = {
                            navController.navigate(SearchRoute)
                        },
                        onNavigateToSettings = {
                            navController.navigate(SettingsRoute)
                        },
                        onEditTags = { song ->
                            tagEditorSong = song
                        },
                        onAlbumClick = { album ->
                            navController.navigate(
                                LibraryDetailRoute(type = DetailType.ALBUM, key = album.id.toString(), title = album.title)
                            )
                        },
                        onArtistClick = { artist ->
                            navController.navigate(
                                LibraryDetailRoute(type = DetailType.ARTIST, key = artist.id.toString(), title = artist.name)
                            )
                        },
                        onGenreClick = { genre ->
                            navController.navigate(
                                LibraryDetailRoute(type = DetailType.GENRE, key = genre.name, title = genre.name)
                            )
                        },
                        onPlaylistClick = { playlist ->
                            navController.navigate(
                                LibraryDetailRoute(type = DetailType.PLAYLIST, key = playlist.id.toString(), title = playlist.name)
                            )
                        },
                        onFolderClick = { folder ->
                            navController.navigate(
                                LibraryDetailRoute(type = DetailType.FOLDER, key = folder.path, title = folder.name)
                            )
                        }
                    )
                }

                composable<LibraryDetailRoute> {
                    SongListDetailScreen(
                        viewModel = hiltViewModel(),
                        onNavigateBack = { navController.popBackStack() },
                        onSongClick = { song, queue ->
                            playerViewModel.playSong(song, queue)
                        }
                    )
                }

                composable<SearchRoute> {
                    SearchScreen(
                        viewModel = searchViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = {
                            navController.popBackStack()
                        },
                        onOpenEqualizer = {
                            navController.navigate(EqualizerRoute)
                        }
                    )
                }

                composable<EqualizerRoute> {
                    EqualizerScreen(
                        equalizerManager = playerViewModel.equalizerManager,
                        onNavigateBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        // Now Playing Fullscreen Overlay
        AnimatedVisibility(
            visible = showNowPlaying && playbackState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            NowPlayingScreen(
                viewModel = playerViewModel,
                onCollapse = { showNowPlaying = false },
                onOpenQueue = { showQueue = true },
                onOpenLyrics = { showLyrics = true },
                onOpenInfo = { showSongInfo = true },
                onOpenSleepTimer = { showSleepTimer = true },
                onOpenSpeedPitch = { showSpeedPitch = true }
            )
        }

        // Bottom Sheets
        if (showQueue) {
            QueueBottomSheet(
                queue = playbackState.queue,
                currentIndex = playbackState.currentQueueIndex,
                onSongSelected = { index ->
                    playbackState.queue.getOrNull(index)?.let { song ->
                        playerViewModel.playSong(song, playbackState.queue)
                    }
                },
                onRemoveFromQueue = { index ->
                    playerViewModel.playerConnection.removeFromQueue(index)
                },
                onReorderQueue = { from, to ->
                    playerViewModel.playerConnection.reorderQueue(from, to)
                },
                onClearQueue = {
                    playerViewModel.playerConnection.clearQueue()
                },
                onSaveAsPlaylist = { name ->
                    playerViewModel.saveQueueAsPlaylist(name)
                },
                onDismiss = { showQueue = false }
            )
        }

        if (showLyrics) {
            LyricsBottomSheet(
                lyrics = currentLyrics,
                currentPositionMs = playbackState.currentPositionMs,
                onSeekTo = { pos -> playerViewModel.seekTo(pos) },
                onAdjustOffset = { offsetDelta -> playerViewModel.adjustLyricsOffset(offsetDelta) },
                onDismiss = { showLyrics = false }
            )
        }

        if (showSongInfo && playbackState.currentSong != null) {
            SongInfoBottomSheet(
                song = playbackState.currentSong!!,
                onDismiss = { showSongInfo = false }
            )
        }

        if (showSleepTimer) {
            SleepTimerBottomSheet(
                sleepTimerManager = playerViewModel.sleepTimerManager,
                onDismissRequest = { showSleepTimer = false }
            )
        }

        if (showSpeedPitch) {
            SpeedPitchBottomSheet(
                viewModel = playerViewModel,
                onDismissRequest = { showSpeedPitch = false }
            )
        }

        tagEditorSong?.let { song ->
            TagEditorBottomSheet(
                song = song,
                onDismissRequest = { tagEditorSong = null },
                onTagsUpdated = {
                    libraryViewModel.refreshLibrary()
                }
            )
        }
    }
}
