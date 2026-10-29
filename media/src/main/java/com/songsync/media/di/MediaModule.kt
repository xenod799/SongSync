package com.songsync.media.di

import android.content.Context
import com.songsync.media.player.EqualizerManager
import com.songsync.media.player.PlayerConnection
import com.songsync.media.player.SleepTimerManager
import com.songsync.media.scanner.MediaStoreScanner
import com.songsync.media.session.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideSleepTimerManager(): SleepTimerManager = SleepTimerManager()

    @Provides
    @Singleton
    fun provideEqualizerManager(): EqualizerManager = EqualizerManager()
}
