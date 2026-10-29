package com.songsync.core.domain.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class Lyrics(
    val songId: Long,
    val isSynced: Boolean,
    val lines: List<LyricLine>,
    val plainText: String,
    val offsetMs: Long = 0L
)
