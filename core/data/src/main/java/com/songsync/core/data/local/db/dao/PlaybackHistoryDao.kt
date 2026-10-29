package com.songsync.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songsync.core.data.local.db.entity.PlaybackHistoryEntity

@Dao
interface PlaybackHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: PlaybackHistoryEntity)

    @Query("DELETE FROM playback_history WHERE songId = :songId")
    suspend fun deleteForSong(songId: Long)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()
}
