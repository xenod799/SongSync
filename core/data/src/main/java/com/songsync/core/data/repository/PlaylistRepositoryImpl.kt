package com.songsync.core.data.repository

import com.songsync.core.data.local.db.dao.PlaylistDao
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.local.db.entity.PlaylistEntity
import com.songsync.core.data.mapper.toDomain
import com.songsync.core.domain.model.Playlist
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.PlaylistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistRepositoryImpl @Inject constructor(
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
) : PlaylistRepository {

    override fun getPlaylists(): Flow<List<Playlist>> =
        playlistDao.getPlaylistsWithStats().map { list ->
            list.map { stats ->
                Playlist(
                    id = stats.id,
                    name = stats.name,
                    createdAtMs = stats.createdAtMs,
                    trackCount = stats.trackCount,
                    totalDurationMs = stats.totalDurationMs
                )
            }
        }

    override fun getPlaylistById(playlistId: Long): Flow<Playlist?> =
        combine(
            playlistDao.getPlaylistWithStatsById(playlistId),
            playlistDao.getPlaylistCoverSongIds(playlistId)
        ) { stats, coverIds ->
            stats?.let {
                Playlist(
                    id = it.id,
                    name = it.name,
                    createdAtMs = it.createdAtMs,
                    trackCount = it.trackCount,
                    totalDurationMs = it.totalDurationMs,
                    coverSongIds = coverIds
                )
            }
        }

    override fun getPlaylistSongs(playlistId: Long): Flow<List<Song>> =
        playlistDao.getSongsForPlaylist(playlistId).map { list -> list.map { it.toDomain() } }

    override suspend fun createPlaylist(name: String): Long =
        playlistDao.insertPlaylist(PlaylistEntity(name = name.trim()))

    override suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName.trim())
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }

    override suspend fun duplicatePlaylist(playlistId: Long, newName: String) {
        val originalSongs = playlistDao.getSongsForPlaylistSync(playlistId)
        val newPlaylistId = playlistDao.insertPlaylist(PlaylistEntity(name = newName.trim()))
        if (originalSongs.isNotEmpty()) {
            playlistDao.addSongsToPlaylist(newPlaylistId, originalSongs.map { it.id })
        }
    }

    override suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        playlistDao.addSongsToPlaylist(playlistId, songIds)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        playlistDao.removeSongFromPlaylist(playlistId, songId)
    }

    override suspend fun reorderPlaylistSongs(playlistId: Long, songIdsInOrder: List<Long>) {
        playlistDao.reorderPlaylist(playlistId, songIdsInOrder)
    }

    override suspend fun importM3uPlaylist(name: String, songPaths: List<String>): Long {
        val allSongs = songDao.getAllSongsList()
        val pathMap = allSongs.associateBy { it.dataPath }
        val matchingSongIds = songPaths.mapNotNull { path ->
            pathMap[path]?.id ?: pathMap[File(path).canonicalPath]?.id
        }

        val playlistId = createPlaylist(name)
        if (matchingSongIds.isNotEmpty()) {
            addSongsToPlaylist(playlistId, matchingSongIds)
        }
        return playlistId
    }

    override suspend fun exportM3uPlaylist(playlistId: Long): String {
        val songs = playlistDao.getSongsForPlaylistSync(playlistId)
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        songs.forEach { song ->
            val durationSec = song.durationMs / 1000
            sb.append("#EXTINF:$durationSec,${song.artistName} - ${song.title}\n")
            sb.append("${song.dataPath}\n")
        }
        return sb.toString()
    }
}
