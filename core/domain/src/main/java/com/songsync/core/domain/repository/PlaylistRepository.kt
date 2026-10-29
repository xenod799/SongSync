package com.songsync.core.domain.repository

import com.songsync.core.domain.model.Playlist
import com.songsync.core.domain.model.Song
import kotlinx.coroutines.flow.Flow

interface PlaylistRepository {
    fun getPlaylists(): Flow<List<Playlist>>
    fun getPlaylistById(playlistId: Long): Flow<Playlist?>
    fun getPlaylistSongs(playlistId: Long): Flow<List<Song>>

    suspend fun createPlaylist(name: String): Long
    suspend fun renamePlaylist(playlistId: Long, newName: String)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun duplicatePlaylist(playlistId: Long, newName: String)
    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>)
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)
    suspend fun reorderPlaylistSongs(playlistId: Long, songIdsInOrder: List<Long>)
    suspend fun importM3uPlaylist(name: String, songPaths: List<String>): Long
    suspend fun exportM3uPlaylist(playlistId: Long): String
}
