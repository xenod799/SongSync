package com.songsync.core.data.mapper

import android.net.Uri
import com.songsync.core.data.local.db.entity.SongEntity
import com.songsync.core.domain.model.Song

fun SongEntity.toDomain(): Song = Song(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artistId = artistId,
    artistName = artistName,
    albumId = albumId,
    albumName = albumName,
    durationMs = durationMs,
    contentUri = Uri.parse(contentUriString),
    albumArtUri = albumArtUriString?.let { Uri.parse(it) },
    dataPath = dataPath,
    trackNumber = trackNumber,
    year = year,
    genre = genre,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    bitrate = bitrate,
    sampleRate = sampleRate,
    dateAddedSec = dateAddedSec,
    dateModifiedSec = dateModifiedSec,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayedTimestampMs = lastPlayedTimestampMs,
    replayGainTrackGainDb = replayGainTrackGainDb,
    replayGainTrackPeak = replayGainTrackPeak
)

fun Song.toEntity(): SongEntity = SongEntity(
    id = id,
    mediaStoreId = mediaStoreId,
    title = title,
    artistId = artistId,
    artistName = artistName,
    albumId = albumId,
    albumName = albumName,
    durationMs = durationMs,
    contentUriString = contentUri.toString(),
    albumArtUriString = albumArtUri?.toString(),
    dataPath = dataPath,
    trackNumber = trackNumber,
    year = year,
    genre = genre,
    sizeBytes = sizeBytes,
    mimeType = mimeType,
    bitrate = bitrate,
    sampleRate = sampleRate,
    dateAddedSec = dateAddedSec,
    dateModifiedSec = dateModifiedSec,
    isFavorite = isFavorite,
    playCount = playCount,
    lastPlayedTimestampMs = lastPlayedTimestampMs,
    replayGainTrackGainDb = replayGainTrackGainDb,
    replayGainTrackPeak = replayGainTrackPeak
)
