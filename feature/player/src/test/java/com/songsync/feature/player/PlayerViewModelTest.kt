package com.songsync.feature.player

import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.LyricsRepository
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.domain.repository.SongRepository
import com.songsync.media.EqualizerManager
import com.songsync.media.PlaybackState
import com.songsync.media.PlayerConnection
import com.songsync.media.SleepTimerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private lateinit var playerConnection: PlayerConnection
    private lateinit var songRepository: SongRepository
    private lateinit var playlistRepository: PlaylistRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var lyricsRepository: LyricsRepository
    private lateinit var sleepTimerManager: SleepTimerManager
    private lateinit var equalizerManager: EqualizerManager
    private lateinit var viewModel: PlayerViewModel

    private val testDispatcher = StandardTestDispatcher()
    private val playbackStateFlow = MutableStateFlow(PlaybackState())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        playerConnection = mock(PlayerConnection::class.java)
        songRepository = mock(SongRepository::class.java)
        playlistRepository = mock(PlaylistRepository::class.java)
        settingsRepository = mock(SettingsRepository::class.java)
        lyricsRepository = mock(LyricsRepository::class.java)
        sleepTimerManager = mock(SleepTimerManager::class.java)
        equalizerManager = mock(EqualizerManager::class.java)

        `when`(playerConnection.playbackState).thenReturn(playbackStateFlow)
        `when`(playlistRepository.getAllPlaylists()).thenReturn(flowOf(emptyList()))

        viewModel = PlayerViewModel(
            playerConnection = playerConnection,
            songRepository = songRepository,
            playlistRepository = playlistRepository,
            settingsRepository = settingsRepository,
            lyricsRepository = lyricsRepository,
            sleepTimerManager = sleepTimerManager,
            equalizerManager = equalizerManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun togglePlayPause_delegatesToPlayerConnection() = runTest {
        viewModel.togglePlayPause()
        verify(playerConnection).togglePlayPause()
    }

    @Test
    fun skipToNext_delegatesToPlayerConnection() = runTest {
        viewModel.skipToNext()
        verify(playerConnection).skipToNext()
    }

    @Test
    fun skipToPrevious_delegatesToPlayerConnection() = runTest {
        viewModel.skipToPrevious()
        verify(playerConnection).skipToPrevious()
    }

    @Test
    fun seekTo_delegatesToPlayerConnection() = runTest {
        viewModel.seekTo(45000L)
        verify(playerConnection).seekTo(45000L)
    }

    @Test
    fun toggleShuffle_delegatesToPlayerConnection() = runTest {
        viewModel.toggleShuffle()
        verify(playerConnection).toggleShuffle()
    }

    @Test
    fun cycleRepeatMode_delegatesToPlayerConnection() = runTest {
        viewModel.cycleRepeatMode()
        verify(playerConnection).cycleRepeatMode()
    }
}
