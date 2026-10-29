package com.songsync.core.domain.repository

import com.songsync.core.domain.model.Lyrics
import kotlinx.coroutines.flow.Flow

interface LyricsRepository {
    fun getLyrics(songId: Long, songPath: String): Flow<Lyrics?>
    suspend fun saveLyricsOffset(songId: Long, offsetMs: Long)
}
