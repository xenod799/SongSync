package com.songsync.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lyrics_offsets")
data class LyricsOffsetEntity(
    @PrimaryKey
    val songId: Long,
    val offsetMs: Long
)
