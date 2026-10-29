package com.songsync.core.data

import com.songsync.core.data.database.dao.SongDao
import com.songsync.core.data.database.entity.SongEntity
import com.songsync.core.data.mapper.toDomainModel
import com.songsync.core.data.repository.SongRepositoryImpl
import com.songsync.core.domain.model.SortField
import com.songsync.core.domain.model.SortOrder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SongRepositoryTest {

    private lateinit var songDao: SongDao
    private lateinit var repository: SongRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        songDao = mock(SongDao::class.java)
        repository = SongRepositoryImpl(songDao, testDispatcher)
    }

    @Test
    fun getSongs_mapsEntitiesToDomainModelsCorrectly() = runTest(testDispatcher) {
        val testEntity = SongEntity(
            id = 101L,
            title = "Test Track",
            artist = "Test Artist",
            artistId = 1L,
            album = "Test Album",
            albumId = 2L,
            duration = 180000L,
            trackNumber = 1,
            year = 2024,
            genre = "Rock",
            data = "/storage/emulated/0/Music/track.mp3",
            size = 5000000L,
            dateAdded = 1700000000L,
            dateModified = 1700001000L,
            isFavorite = true,
            playCount = 5,
            lastPlayedTimestamp = 1700002000L,
            albumArtUri = "content://media/external/audio/albumart/2",
            replayGainTrackGain = -3.5f,
            replayGainTrackPeak = 0.98f
        )

        `when`(songDao.getAllSongs()).thenReturn(flowOf(listOf(testEntity)))

        val songs = repository.getSongs(SortOrder(SortField.TITLE, true)).first()

        assertEquals(1, songs.size)
        val song = songs.first()
        assertEquals(101L, song.id)
        assertEquals("Test Track", song.title)
        assertEquals("Test Artist", song.artist)
        assertEquals("Test Album", song.album)
        assertEquals(180000L, song.duration)
        assertTrue(song.isFavorite)
        assertEquals(5, song.playCount)
    }

    @Test
    fun toggleFavorite_invokesDaoUpdate() = runTest(testDispatcher) {
        repository.toggleFavorite(101L, true)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(songDao).updateFavorite(101L, true)
    }

    @Test
    fun incrementPlayCount_invokesDaoWithCurrentTimestamp() = runTest(testDispatcher) {
        val timestamp = 1700005000L
        repository.incrementPlayCount(101L, timestamp)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(songDao).incrementPlayCount(101L, timestamp)
    }
}
