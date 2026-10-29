package com.songsync.core.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.songsync.core.data.local.db.entity.LyricsOffsetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LyricsDao {
    @Query("SELECT offsetMs FROM lyrics_offsets WHERE songId = :songId")
    fun getOffset(songId: Long): Flow<Long?>

    @Query("SELECT offsetMs FROM lyrics_offsets WHERE songId = :songId")
    suspend fun getOffsetSync(songId: Long): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOffset(offset: LyricsOffsetEntity)
}
