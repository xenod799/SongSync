package com.songsync.app.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import com.songsync.feature.library.EqualizerScreen
import com.songsync.feature.library.LibraryScreen
import com.songsync.feature.library.LibraryViewModel
import com.songsync.feature.library.SearchScreen
import com.songsync.feature.library.SearchViewModel
import com.songsync.feature.library.tageditor.TagEditorBottomSheet
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
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val playbackState by playerViewModel.playbackState.collectAsState()

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
            route = LibraryRoute::class.qualifiedName ?: "LibraryRoute",
            title = "Library",
            icon = Icons.Rounded.LibraryMusic
        ),
        NavItem(
            route = SearchRoute::class.qualifiedName ?: "SearchRoute",
            title = "Search",
            icon = Icons.Rounded.Search
        ),
        NavItem(
            route = SettingsRoute::class.qualifiedName ?: "Settings",
            title = "Settings",
            icon = Icons.Rounded.Settings
        )
    )

    val isTopLevelRoute = currentRoute == LibraryRoute::class.qualifiedName ||
            currentRoute == SearchRoute::class.qualifiedName ||
            currentRoute == SettingsRoute::class.qualifiedName

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (isOnboardingCompleted && isTopLevelRoute) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AnimatedVisibility(
                            visible = playbackState.currentSong != null,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it }
                        ) {
                            playbackState.currentSong?.let { song ->
                                MiniPlayer(
                                    song = song,
                                    isPlaying = playbackState.isPlaying,
                                    progress = playbackState.progress,
                                    onPlayPauseClick = { playerViewModel.togglePlayPause() },
                                    onNextClick = { playerViewModel.skipToNext() },
                                    onPreviousClick = { playerViewModel.skipToPrevious() },
                                    onExpandClick = { showNowPlaying = true },
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                )
                            }
                        }

                        FloatingNavBar(
                            items = navItems,
                            currentRoute = currentRoute ?: (LibraryRoute::class.qualifiedName ?: ""),
                            onItemClick = { route ->
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
            ) {
                composable<OnboardingRoute> {
                    OnboardingScreen(
                        onOnboardingComplete = {
                            onOnboardingCompleted()
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
                        onEditTags = { song ->
                            tagEditorSong = song
                        }
                    )
                }

                composable<SearchRoute> {
                    SearchScreen(
                        viewModel = searchViewModel,
                        onSongClick = { song ->
                            playerViewModel.playSong(song, listOf(song))
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable<SettingsRoute> {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateToEqualizer = {
                            navController.navigate(EqualizerRoute)
                        }
                    )
                }

                composable<EqualizerRoute> {
                    EqualizerScreen(
                        equalizerManager = playerViewModel.equalizerManager,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }

        // Now Playing Overlay
        AnimatedVisibility(
            visible = showNowPlaying && playbackState.currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            playbackState.currentSong?.let { song ->
                NowPlayingScreen(
                    viewModel = playerViewModel,
                    onCollapse = { showNowPlaying = false },
                    onOpenQueue = { showQueue = true },
                    onOpenLyrics = { showLyrics = true },
                    onOpenSongInfo = { showSongInfo = true },
                    onOpenSleepTimer = { showSleepTimer = true },
                    onOpenSpeedPitch = { showSpeedPitch = true }
                )
            }
        }

        // Sheets
        if (showQueue) {
            QueueBottomSheet(
                viewModel = playerViewModel,
                onDismissRequest = { showQueue = false }
            )
        }

        if (showLyrics) {
            LyricsBottomSheet(
                viewModel = playerViewModel,
                onDismissRequest = { showLyrics = false }
            )
        }

        if (showSongInfo && playbackState.currentSong != null) {
            SongInfoBottomSheet(
                song = playbackState.currentSong!!,
                onDismissRequest = { showSongInfo = false }
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
