package com.songsync.core.data

import android.net.Uri
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.local.db.entity.SongEntity
import com.songsync.core.data.repository.SongRepositoryImpl
import com.songsync.core.domain.model.SortBy
import com.songsync.core.domain.model.SortOrder
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SongRepositoryTest {

    private lateinit var songDao: SongDao
    private lateinit var repository: SongRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        songDao = mockk(relaxed = true)
        repository = SongRepositoryImpl(songDao)
    }

    @After
    fun tearDown() {
        unmockkStatic(Uri::class)
    }

    @Test
    fun getSongs_mapsEntitiesToDomainModelsCorrectly() = runTest(testDispatcher) {
        val testEntity = SongEntity(
            id = 101L,
            mediaStoreId = 1001L,
            title = "Test Track",
            artistId = 1L,
            artistName = "Test Artist",
            albumId = 2L,
            albumName = "Test Album",
            durationMs = 180000L,
            contentUriString = "content://media/external/audio/media/1001",
            albumArtUriString = "content://media/external/audio/albumart/2",
            dataPath = "/storage/emulated/0/Music/track.mp3",
            trackNumber = 1,
            year = 2024,
            genre = "Rock",
            sizeBytes = 5000000L,
            mimeType = "audio/mpeg",
            bitrate = 320000,
            sampleRate = 44100,
            dateAddedSec = 1700000000L,
            dateModifiedSec = 1700001000L,
            isFavorite = true,
            playCount = 5,
            lastPlayedTimestampMs = 1700002000L,
            replayGainTrackGainDb = -3.5f,
            replayGainTrackPeak = 0.98f
        )

        every { songDao.getAllSongsByTitleAsc() } returns flowOf(listOf(testEntity))

        val songs = repository.getSongs(SortOrder(SortBy.TITLE, true)).first()

        assertEquals(1, songs.size)
        val song = songs.first()
        assertEquals(101L, song.id)
        assertEquals("Test Track", song.title)
        assertEquals("Test Artist", song.artistName)
        assertEquals("Test Album", song.albumName)
        assertEquals(180000L, song.durationMs)
        assertTrue(song.isFavorite)
        assertEquals(5, song.playCount)
    }

    @Test
    fun toggleFavorite_invokesDaoUpdate() = runTest(testDispatcher) {
        repository.toggleFavorite(101L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { songDao.updateFavorite(101L, true) }
    }

    @Test
    fun recordSongPlay_invokesDaoWithCurrentTimestamp() = runTest(testDispatcher) {
        val timestamp = 1700005000L
        repository.recordSongPlay(101L, timestamp)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { songDao.incrementPlayCount(101L, timestamp) }
    }
}
