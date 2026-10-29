package com.songsync.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "songs",
    indices = [
        Index(value = ["mediaStoreId"], unique = true),
        Index(value = ["artistId"]),
        Index(value = ["albumId"]),
        Index(value = ["title"]),
        Index(value = ["isFavorite"]),
        Index(value = ["dateAddedSec"])
    ]
)
data class SongEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mediaStoreId: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumName: String,
    val durationMs: Long,
    val contentUriString: String,
    val albumArtUriString: String?,
    val dataPath: String,
    val trackNumber: Int,
    val year: Int,
    val genre: String?,
    val sizeBytes: Long,
    val mimeType: String,
    val bitrate: Int,
    val sampleRate: Int,
    val dateAddedSec: Long,
    val dateModifiedSec: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0,
    val lastPlayedTimestampMs: Long = 0L,
    val replayGainTrackGainDb: Float? = null,
    val replayGainTrackPeak: Float? = null
)
