package com.songsync.core.data.repository

import android.net.Uri
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.mapper.toDomain
import com.songsync.core.domain.model.Album
import com.songsync.core.domain.model.Artist
import com.songsync.core.domain.model.Folder
import com.songsync.core.domain.model.Genre
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.model.SortBy
import com.songsync.core.domain.model.SortOrder
import com.songsync.core.domain.repository.SongRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SongRepositoryImpl @Inject constructor(
    private val songDao: SongDao
) : SongRepository {

    override fun getSongs(sortOrder: SortOrder): Flow<List<Song>> {
        val flow = when (sortOrder.sortBy) {
            SortBy.TITLE -> if (sortOrder.isAscending) songDao.getAllSongsByTitleAsc() else songDao.getAllSongsByTitleDesc()
            SortBy.ARTIST -> if (sortOrder.isAscending) songDao.getAllSongsByArtistAsc() else songDao.getAllSongsByArtistDesc()
            SortBy.ALBUM -> if (sortOrder.isAscending) songDao.getAllSongsByAlbumAsc() else songDao.getAllSongsByAlbumDesc()
            SortBy.DATE_ADDED -> if (sortOrder.isAscending) songDao.getAllSongsByDateAddedAsc() else songDao.getAllSongsByDateAddedDesc()
            SortBy.DURATION -> if (sortOrder.isAscending) songDao.getAllSongsByDurationAsc() else songDao.getAllSongsByDurationDesc()
            SortBy.PLAY_COUNT -> songDao.getAllSongsByPlayCountDesc()
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override fun getSongById(id: Long): Flow<Song?> =
        songDao.getSongById(id).map { it?.toDomain() }

    override fun getFavorites(): Flow<List<Song>> =
        songDao.getFavoriteSongs().map { list -> list.map { it.toDomain() } }

    override fun getRecentlyAdded(limit: Int): Flow<List<Song>> =
        songDao.getRecentlyAdded(limit).map { list -> list.map { it.toDomain() } }

    override fun getRecentlyPlayed(limit: Int): Flow<List<Song>> =
        songDao.getRecentlyPlayed(limit).map { list -> list.map { it.toDomain() } }

    override fun getMostPlayed(limit: Int): Flow<List<Song>> =
        songDao.getMostPlayed(limit).map { list -> list.map { it.toDomain() } }

    override fun getAlbums(): Flow<List<Album>> =
        songDao.getAlbums().map { list ->
            list.map { summary ->
                Album(
                    id = summary.albumId,
                    title = summary.albumName,
                    artistId = summary.artistId,
                    artistName = summary.artistName,
                    songCount = summary.songCount,
                    year = summary.year,
                    artworkUri = summary.albumArtUriString?.let { Uri.parse(it) }
                )
            }
        }

    override fun getAlbumSongs(albumId: Long): Flow<List<Song>> =
        songDao.getSongsByAlbum(albumId).map { list -> list.map { it.toDomain() } }

    override fun getArtists(): Flow<List<Artist>> =
        songDao.getArtists().map { list ->
            list.map { summary ->
                Artist(
                    id = summary.artistId,
                    name = summary.artistName,
                    trackCount = summary.trackCount,
                    albumCount = summary.albumCount
                )
            }
        }

    override fun getArtistSongs(artistId: Long): Flow<List<Song>> =
        songDao.getSongsByArtist(artistId).map { list -> list.map { it.toDomain() } }

    override fun getGenres(): Flow<List<Genre>> =
        songDao.getGenres().map { list ->
            list.map { summary ->
                Genre(
                    name = summary.genre,
                    songCount = summary.songCount
                )
            }
        }

    override fun getGenreSongs(genreName: String): Flow<List<Song>> =
        songDao.getSongsByGenre(genreName).map { list -> list.map { it.toDomain() } }

    override fun getFolders(): Flow<List<Folder>> =
        songDao.getAllSongsByTitleAsc().map { songEntities ->
            songEntities
                .groupBy { entity ->
                    val file = File(entity.dataPath)
                    file.parent ?: "/"
                }
                .map { (folderPath, songs) ->
                    val name = File(folderPath).name.ifEmpty { "Root" }
                    Folder(
                        path = folderPath,
                        name = name,
                        songCount = songs.size
                    )
                }
                .sortedBy { it.name.lowercase() }
        }

    override fun getFolderSongs(folderPath: String): Flow<List<Song>> =
        songDao.getAllSongsByTitleAsc().map { songEntities ->
            songEntities
                .filter { File(it.dataPath).parent == folderPath }
                .map { it.toDomain() }
        }

    override fun search(query: String): Flow<List<Song>> =
        songDao.searchSongs(query).map { list -> list.map { it.toDomain() } }

    override suspend fun toggleFavorite(songId: Long, isFavorite: Boolean) {
        songDao.updateFavorite(songId, isFavorite)
    }

    override suspend fun recordSongPlay(songId: Long, playedAtMs: Long) {
        songDao.incrementPlayCount(songId, playedAtMs)
    }

    override suspend fun updateSongTags(
        songId: Long,
        title: String,
        artist: String,
        album: String,
        genre: String?,
        year: Int,
        trackNumber: Int
    ) {
        songDao.updateSongMetadata(
            songId = songId,
            title = title,
            artist = artist,
            album = album,
            genre = genre,
            year = year,
            trackNumber = trackNumber
        )
    }

    override suspend fun deleteSongs(songIds: List<Long>) {
        songDao.deleteByIds(songIds)
    }

    override suspend fun getSongCount(): Int = songDao.getSongCount()
}
