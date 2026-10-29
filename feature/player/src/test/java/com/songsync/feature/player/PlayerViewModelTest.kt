package com.songsync.feature.player

import android.content.Context
import com.songsync.core.domain.model.UserSettings
import com.songsync.core.domain.repository.LyricsRepository
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.domain.repository.SongRepository
import com.songsync.media.player.EqualizerManager
import com.songsync.media.player.PlaybackState
import com.songsync.media.player.PlayerConnection
import com.songsync.media.player.SleepTimerManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private lateinit var context: Context
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

        context = mockk(relaxed = true)
        playerConnection = mockk(relaxed = true)
        songRepository = mockk(relaxed = true)
        playlistRepository = mockk(relaxed = true)
        settingsRepository = mockk(relaxed = true)
        lyricsRepository = mockk(relaxed = true)
        sleepTimerManager = mockk(relaxed = true)
        equalizerManager = mockk(relaxed = true)

        every { playerConnection.playbackState } returns playbackStateFlow
        every { settingsRepository.settingsFlow } returns flowOf(UserSettings())
        every { playlistRepository.getPlaylists() } returns flowOf(emptyList())

        viewModel = PlayerViewModel(
            context = context,
            playerConnection = playerConnection,
            sleepTimerManager = sleepTimerManager,
            equalizerManager = equalizerManager,
            songRepository = songRepository,
            lyricsRepository = lyricsRepository,
            playlistRepository = playlistRepository,
            settingsRepository = settingsRepository
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun togglePlayPause_delegatesToPlayerConnection() = runTest {
        viewModel.togglePlayPause()
        verify { playerConnection.togglePlayPause() }
    }

    @Test
    fun skipToNext_delegatesToPlayerConnection() = runTest {
        viewModel.skipToNext()
        verify { playerConnection.skipToNext() }
    }

    @Test
    fun skipToPrevious_delegatesToPlayerConnection() = runTest {
        viewModel.skipToPrevious()
        verify { playerConnection.skipToPrevious() }
    }

    @Test
    fun seekTo_delegatesToPlayerConnection() = runTest {
        viewModel.seekTo(45000L)
        verify { playerConnection.seekTo(45000L) }
    }

    @Test
    fun toggleShuffle_delegatesToPlayerConnection() = runTest {
        viewModel.toggleShuffle()
        verify { playerConnection.toggleShuffle() }
    }

    @Test
    fun cycleRepeatMode_delegatesToPlayerConnection() = runTest {
        viewModel.cycleRepeatMode()
        verify { playerConnection.cycleRepeatMode() }
    }
}
