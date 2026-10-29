package com.songsync.core.common.dispatcher

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val dispatcher: SongSyncDispatchers)

enum class SongSyncDispatchers {
    Default,
    IO,
    Main
}
