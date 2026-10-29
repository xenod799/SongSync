package com.songsync.core.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "playback_history",
    foreignKeys = [
        ForeignKey(
            entity = SongEntity::class,
            parentColumns = ["id"],
            childColumns = ["songId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["songId"]),
        Index(value = ["playedAtMs"])
    ]
)
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val songId: Long,
    val playedAtMs: Long = System.currentTimeMillis()
)
