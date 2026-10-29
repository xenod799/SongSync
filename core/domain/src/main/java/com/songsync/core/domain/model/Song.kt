package com.songsync.core.domain.model

import android.net.Uri

data class Song(
    val id: Long,
    val mediaStoreId: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumName: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri?,
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
