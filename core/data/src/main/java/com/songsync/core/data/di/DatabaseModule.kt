package com.songsync.core.data.di

import android.content.Context
import androidx.room.Room
import com.songsync.core.data.local.db.SongSyncDatabase
import com.songsync.core.data.local.db.dao.LyricsDao
import com.songsync.core.data.local.db.dao.PlaybackHistoryDao
import com.songsync.core.data.local.db.dao.PlaylistDao
import com.songsync.core.data.local.db.dao.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SongSyncDatabase {
        return Room.databaseBuilder(
            context,
            SongSyncDatabase::class.java,
            "songsync.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSongDao(database: SongSyncDatabase): SongDao = database.songDao()

    @Provides
    fun providePlaylistDao(database: SongSyncDatabase): PlaylistDao = database.playlistDao()

    @Provides
    fun providePlaybackHistoryDao(database: SongSyncDatabase): PlaybackHistoryDao = database.playbackHistoryDao()

    @Provides
    fun provideLyricsDao(database: SongSyncDatabase): LyricsDao = database.lyricsDao()
}
