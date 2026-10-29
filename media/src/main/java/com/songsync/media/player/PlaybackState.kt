package com.songsync.media.player

import com.songsync.core.domain.model.Song

data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedPositionMs: Long = 0L,
    val shuffleModeEnabled: Boolean = false,
    val repeatMode: Int = 0, // 0 = off, 1 = one, 2 = all
    val playbackSpeed: Float = 1.0f,
    val playbackPitch: Float = 1.0f,
    val queue: List<Song> = emptyList(),
    val currentQueueIndex: Int = -1,
    val sleepTimerState: SleepTimerState = SleepTimerState.Inactive
)
