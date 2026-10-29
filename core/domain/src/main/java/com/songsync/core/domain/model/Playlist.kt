package com.songsync.core.domain.model

data class Playlist(
    val id: Long,
    val name: String,
    val createdAtMs: Long,
    val trackCount: Int,
    val totalDurationMs: Long,
    val coverSongIds: List<Long> = emptyList()
)
