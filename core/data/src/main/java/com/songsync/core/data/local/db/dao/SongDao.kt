package com.songsync.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.songsync.core.data.local.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class AlbumSummary(
    val albumId: Long,
    val albumName: String,
    val artistId: Long,
    val artistName: String,
    val songCount: Int,
    val year: Int,
    val albumArtUriString: String?
)

data class ArtistSummary(
    val artistId: Long,
    val artistName: String,
    val trackCount: Int,
    val albumCount: Int
)

data class GenreSummary(
    val genre: String,
    val songCount: Int
)

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun getAllSongsByTitleAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE DESC")
    fun getAllSongsByTitleDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY artistName COLLATE NOCASE ASC, albumName COLLATE NOCASE ASC, trackNumber ASC")
    fun getAllSongsByArtistAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY artistName COLLATE NOCASE DESC")
    fun getAllSongsByArtistDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY albumName COLLATE NOCASE ASC, trackNumber ASC")
    fun getAllSongsByAlbumAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY albumName COLLATE NOCASE DESC")
    fun getAllSongsByAlbumDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAddedSec DESC")
    fun getAllSongsByDateAddedDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAddedSec ASC")
    fun getAllSongsByDateAddedAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY durationMs DESC")
    fun getAllSongsByDurationDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY durationMs ASC")
    fun getAllSongsByDurationAsc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY playCount DESC")
    fun getAllSongsByPlayCountDesc(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :songId")
    fun getSongById(songId: Long): Flow<SongEntity?>

    @Query("SELECT * FROM songs WHERE id = :songId")
    suspend fun getSongByIdSync(songId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE mediaStoreId = :mediaStoreId LIMIT 1")
    suspend fun getSongByMediaStoreId(mediaStoreId: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs ORDER BY dateAddedSec DESC LIMIT :limit")
    fun getRecentlyAdded(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE lastPlayedTimestampMs > 0 ORDER BY lastPlayedTimestampMs DESC LIMIT :limit")
    fun getRecentlyPlayed(limit: Int): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE playCount > 0 ORDER BY playCount DESC LIMIT :limit")
    fun getMostPlayed(limit: Int): Flow<List<SongEntity>>

    @Query("""
        SELECT albumId, albumName, artistId, artistName, COUNT(*) as songCount, MAX(year) as year, 
        MAX(albumArtUriString) as albumArtUriString 
        FROM songs 
        GROUP BY albumId 
        ORDER BY albumName COLLATE NOCASE ASC
    """)
    fun getAlbums(): Flow<List<AlbumSummary>>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber ASC, title COLLATE NOCASE ASC")
    fun getSongsByAlbum(albumId: Long): Flow<List<SongEntity>>

    @Query("""
        SELECT artistId, artistName, COUNT(*) as trackCount, COUNT(DISTINCT albumId) as albumCount 
        FROM songs 
        GROUP BY artistId 
        ORDER BY artistName COLLATE NOCASE ASC
    """)
    fun getArtists(): Flow<List<ArtistSummary>>

    @Query("SELECT * FROM songs WHERE artistId = :artistId ORDER BY albumName COLLATE NOCASE ASC, trackNumber ASC, title COLLATE NOCASE ASC")
    fun getSongsByArtist(artistId: Long): Flow<List<SongEntity>>

    @Query("""
        SELECT genre, COUNT(*) as songCount 
        FROM songs 
        WHERE genre IS NOT NULL AND TRIM(genre) != '' 
        GROUP BY genre 
        ORDER BY genre COLLATE NOCASE ASC
    """)
    fun getGenres(): Flow<List<GenreSummary>>

    @Query("SELECT * FROM songs WHERE genre = :genreName ORDER BY title COLLATE NOCASE ASC")
    fun getSongsByGenre(genreName: String): Flow<List<SongEntity>>

    @Query("""
        SELECT * FROM songs 
        WHERE title LIKE '%' || :query || '%' 
           OR artistName LIKE '%' || :query || '%' 
           OR albumName LIKE '%' || :query || '%' 
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs")
    suspend fun getAllSongsList(): List<SongEntity>

    @Query("SELECT mediaStoreId FROM songs")
    suspend fun getAllMediaStoreIds(): List<Long>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)

    @Update
    suspend fun updateSong(song: SongEntity)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :songId")
    suspend fun updateFavorite(songId: Long, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1, lastPlayedTimestampMs = :playedAtMs WHERE id = :songId")
    suspend fun incrementPlayCount(songId: Long, playedAtMs: Long)

    @Query("""
        UPDATE songs 
        SET title = :title, artistName = :artist, albumName = :album, genre = :genre, year = :year, trackNumber = :trackNumber 
        WHERE id = :songId
    """)
    suspend fun updateSongMetadata(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int,
        trackNumber: Int
    )

    @Query("DELETE FROM songs WHERE mediaStoreId IN (:mediaStoreIds)")
    suspend fun deleteByMediaStoreIds(mediaStoreIds: List<Long>)

    @Query("DELETE FROM songs WHERE id IN (:songIds)")
    suspend fun deleteByIds(songIds: List<Long>)
}
