package com.songsync.core.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.songsync.core.data.local.db.dao.LyricsDao
import com.songsync.core.data.local.db.dao.PlaybackHistoryDao
import com.songsync.core.data.local.db.dao.PlaylistDao
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.local.db.entity.LyricsOffsetEntity
import com.songsync.core.data.local.db.entity.PlaybackHistoryEntity
import com.songsync.core.data.local.db.entity.PlaylistEntity
import com.songsync.core.data.local.db.entity.PlaylistSongCrossRef
import com.songsync.core.data.local.db.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        PlaybackHistoryEntity::class,
        LyricsOffsetEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SongSyncDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playbackHistoryDao(): PlaybackHistoryDao
    abstract fun lyricsDao(): LyricsDao
}
