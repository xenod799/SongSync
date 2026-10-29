package com.songsync.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.songsync.core.data.local.db.entity.PlaylistEntity
import com.songsync.core.data.local.db.entity.PlaylistSongCrossRef
import com.songsync.core.data.local.db.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithStats(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val trackCount: Int,
    val totalDurationMs: Long
)

@Dao
interface PlaylistDao {
    @Query("""
        SELECT p.id, p.name, p.createdAtMs, 
               COUNT(c.songId) as trackCount, 
               COALESCE(SUM(s.durationMs), 0) as totalDurationMs
        FROM playlists p
        LEFT JOIN playlist_song_cross_ref c ON p.id = c.playlistId
        LEFT JOIN songs s ON c.songId = s.id
        GROUP BY p.id
        ORDER BY p.name COLLATE NOCASE ASC
    """)
    fun getPlaylistsWithStats(): Flow<List<PlaylistWithStats>>

    @Query("""
        SELECT p.id, p.name, p.createdAtMs, 
               COUNT(c.songId) as trackCount, 
               COALESCE(SUM(s.durationMs), 0) as totalDurationMs
        FROM playlists p
        LEFT JOIN playlist_song_cross_ref c ON p.id = c.playlistId
        LEFT JOIN songs s ON c.songId = s.id
        WHERE p.id = :playlistId
        GROUP BY p.id
    """)
    fun getPlaylistWithStatsById(playlistId: Long): Flow<PlaylistWithStats?>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref c ON s.id = c.songId
        WHERE c.playlistId = :playlistId
        ORDER BY c.orderInPlaylist ASC
    """)
    fun getSongsForPlaylist(playlistId: Long): Flow<List<SongEntity>>

    @Query("""
        SELECT s.* FROM songs s
        INNER JOIN playlist_song_cross_ref c ON s.id = c.songId
        WHERE c.playlistId = :playlistId
        ORDER BY c.orderInPlaylist ASC
    """)
    suspend fun getSongsForPlaylistSync(playlistId: Long): List<SongEntity>

    @Query("SELECT songId FROM playlist_song_cross_ref WHERE playlistId = :playlistId ORDER BY orderInPlaylist ASC LIMIT 4")
    fun getPlaylistCoverSongIds(playlistId: Long): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(refs: List<PlaylistSongCrossRef>)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun clearPlaylistSongs(playlistId: Long)

    @Query("SELECT COALESCE(MAX(orderInPlaylist), -1) FROM playlist_song_cross_ref WHERE playlistId = :playlistId")
    suspend fun getMaxOrderInPlaylist(playlistId: Long): Int

    @Transaction
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val maxOrder = getMaxOrderInPlaylist(playlistId)
        val refs = songIds.mapIndexed { index, songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderInPlaylist = maxOrder + 1 + index
            )
        }
        insertCrossRefs(refs)
    }

    @Transaction
    suspend fun reorderPlaylist(playlistId: Long, orderedSongIds: List<Long>) {
        clearPlaylistSongs(playlistId)
        val refs = orderedSongIds.mapIndexed { index, songId ->
            PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                orderInPlaylist = index
            )
        }
        insertCrossRefs(refs)
    }

    @Query("SELECT * FROM playlists")
    suspend fun getAllPlaylists(): List<PlaylistEntity>

    @Query("SELECT * FROM playlist_song_cross_ref")
    suspend fun getAllCrossRefs(): List<PlaylistSongCrossRef>
}
