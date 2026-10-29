package com.songsync.media.scanner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.local.db.entity.SongEntity
import com.songsync.core.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val settingsRepository: SettingsRepository
) {

    suspend fun scanAndSync(onProgress: suspend (progressPercent: Int) -> Unit = {}): Int = withContext(Dispatchers.IO) {
        val settings = settingsRepository.settingsFlow.first()
        val minDurationMs = settings.minTrackDurationSeconds * 1000L
        val excludedFolders = settings.excludedFolders

        val collectionUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ARTIST_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATE_MODIFIED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(minDurationMs.toString())
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val foundMediaStoreIds = mutableSetOf<Long>()
        val songsToInsert = mutableListOf<SongEntity>()

        val existingIds = songDao.getAllMediaStoreIds().toSet()
        val existingSongs = songDao.getAllSongsList().associateBy { it.mediaStoreId }

        context.contentResolver.query(
            collectionUri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val totalCount = cursor.count
            var processed = 0

            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val artistIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)
            val yearCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)
            val mimeTypeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)
            val dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)

            while (cursor.moveToNext()) {
                val mediaId = cursor.getLong(idCol)
                foundMediaStoreIds.add(mediaId)

                val dataPath = cursor.getString(dataCol) ?: ""
                val parentFolder = File(dataPath).parent ?: ""

                // Check if folder is excluded
                if (excludedFolders.any { parentFolder.startsWith(it) }) {
                    continue
                }

                val dateModified = cursor.getLong(dateModifiedCol)
                val existing = existingSongs[mediaId]

                // If file is already indexed and unmodified, retain existing entity state
                if (existing != null && existing.dateModifiedSec == dateModified && existing.dataPath == dataPath) {
                    processed++
                    if (totalCount > 0 && processed % 50 == 0) {
                        onProgress((processed * 100) / totalCount)
                    }
                    continue
                }

                val title = cursor.getString(titleCol) ?: File(dataPath).nameWithoutExtension
                val artist = cursor.getString(artistCol)?.takeIf { it != "<unknown>" } ?: "Unknown Artist"
                val artistId = cursor.getLong(artistIdCol)
                val album = cursor.getString(albumCol)?.takeIf { it != "<unknown>" } ?: "Unknown Album"
                val albumId = cursor.getLong(albumIdCol)
                val duration = cursor.getLong(durationCol)
                val track = cursor.getInt(trackCol)
                val year = cursor.getInt(yearCol)
                val mimeType = cursor.getString(mimeTypeCol) ?: "audio/mpeg"
                val size = cursor.getLong(sizeCol)
                val dateAdded = cursor.getLong(dateAddedCol)

                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mediaId)
                val artUri = ContentUris.withAppendedId(
                    Uri.parse("content://media/external/audio/albumart"),
                    albumId
                )

                val (replayGainGain, replayGainPeak, genre, bitrate, sampleRate) = readAudioExtras(dataPath)

                val songEntity = SongEntity(
                    id = existing?.id ?: 0L,
                    mediaStoreId = mediaId,
                    title = title,
                    artistId = artistId,
                    artistName = artist,
                    albumId = albumId,
                    albumName = album,
                    durationMs = duration,
                    contentUriString = contentUri.toString(),
                    albumArtUriString = artUri.toString(),
                    dataPath = dataPath,
                    trackNumber = track,
                    year = year,
                    genre = genre,
                    sizeBytes = size,
                    mimeType = mimeType,
                    bitrate = bitrate,
                    sampleRate = sampleRate,
                    dateAddedSec = dateAdded,
                    dateModifiedSec = dateModified,
                    isFavorite = existing?.isFavorite ?: false,
                    playCount = existing?.playCount ?: 0,
                    lastPlayedTimestampMs = existing?.lastPlayedTimestampMs ?: 0L,
                    replayGainTrackGainDb = replayGainGain,
                    replayGainTrackPeak = replayGainPeak
                )

                songsToInsert.add(songEntity)
                processed++

                if (totalCount > 0 && processed % 25 == 0) {
                    onProgress((processed * 100) / totalCount)
                }
            }
        }

        if (songsToInsert.isNotEmpty()) {
            songDao.insertSongs(songsToInsert)
        }

        // Clean up deleted files from database
        val deletedIds = existingIds - foundMediaStoreIds
        if (deletedIds.isNotEmpty()) {
            songDao.deleteByMediaStoreIds(deletedIds.toList())
        }

        onProgress(100)
        return@withContext songsToInsert.size
    }

    private data class AudioExtras(
        val replayGainGain: Float?,
        val replayGainPeak: Float?,
        val genre: String?,
        val bitrate: Int,
        val sampleRate: Int
    )

    private fun readAudioExtras(filePath: String): AudioExtras {
        val retriever = MediaMetadataRetriever()
        var genre: String? = null
        var bitrate = 0
        var sampleRate = 44100
        var trackGain: Float? = null
        var trackPeak: Float? = null

        try {
            retriever.setDataSource(filePath)
            genre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull() ?: 0

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                sampleRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull() ?: 44100
            }

            // ReplayGain tag comments are not exposed by MediaMetadataRetriever; skip extraction
        } catch (_: Exception) {
            // Unreadable or corrupted audio file
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }

        return AudioExtras(trackGain, trackPeak, genre, bitrate, sampleRate)
    }

    }
