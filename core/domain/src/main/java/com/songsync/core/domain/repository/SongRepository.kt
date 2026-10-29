package com.songsync.core.domain.repository

import com.songsync.core.domain.model.Album
import com.songsync.core.domain.model.Artist
import com.songsync.core.domain.model.Folder
import com.songsync.core.domain.model.Genre
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.model.SortOrder
import kotlinx.coroutines.flow.Flow

interface SongRepository {
    fun getSongs(sortOrder: SortOrder): Flow<List<Song>>
    fun getSongById(id: Long): Flow<Song?>
    fun getFavorites(): Flow<List<Song>>
    fun getRecentlyAdded(limit: Int = 50): Flow<List<Song>>
    fun getRecentlyPlayed(limit: Int = 50): Flow<List<Song>>
    fun getMostPlayed(limit: Int = 50): Flow<List<Song>>

    fun getAlbums(): Flow<List<Album>>
    fun getAlbumSongs(albumId: Long): Flow<List<Song>>
    fun getArtists(): Flow<List<Artist>>
    fun getArtistSongs(artistId: Long): Flow<List<Song>>
    fun getGenres(): Flow<List<Genre>>
    fun getGenreSongs(genreName: String): Flow<List<Song>>
    fun getFolders(): Flow<List<Folder>>
    fun getFolderSongs(folderPath: String): Flow<List<Song>>

    fun search(query: String): Flow<List<Song>>

    suspend fun toggleFavorite(songId: Long, isFavorite: Boolean)
    suspend fun recordSongPlay(songId: Long, playedAtMs: Long)
    suspend fun updateSongTags(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int,
        trackNumber: Int
    )
    suspend fun deleteSongs(songIds: List<Long>)
    suspend fun getSongCount(): Int
}
