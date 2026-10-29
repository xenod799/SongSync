package com.songsync.core.data.di

import com.songsync.core.data.repository.LyricsRepositoryImpl
import com.songsync.core.data.repository.PlaylistRepositoryImpl
import com.songsync.core.data.repository.SettingsRepositoryImpl
import com.songsync.core.data.repository.SongRepositoryImpl
import com.songsync.core.domain.repository.LyricsRepository
import com.songsync.core.domain.repository.PlaylistRepository
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.domain.repository.SongRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSongRepository(impl: SongRepositoryImpl): SongRepository

    @Binds
    @Singleton
    abstract fun bindPlaylistRepository(impl: PlaylistRepositoryImpl): PlaylistRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindLyricsRepository(impl: LyricsRepositoryImpl): LyricsRepository
}
