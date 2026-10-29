package com.songsync.core.data.di

import com.songsync.core.common.dispatcher.Dispatcher
import com.songsync.core.common.dispatcher.SongSyncDispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {

    @Provides
    @Dispatcher(SongSyncDispatchers.IO)
    fun provideIODispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Dispatcher(SongSyncDispatchers.Default)
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @Dispatcher(SongSyncDispatchers.Main)
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
