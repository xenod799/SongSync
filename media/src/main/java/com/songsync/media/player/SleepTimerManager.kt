package com.songsync.media.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed interface SleepTimerState {
    data object Inactive : SleepTimerState
    data class Active(
        val remainingMs: Long,
        val totalMs: Long,
        val endOfTrack: Boolean,
        val fadeOut: Boolean
    ) : SleepTimerState
}

@Singleton
class SleepTimerManager @Inject constructor() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null

    private val _timerState = MutableStateFlow<SleepTimerState>(SleepTimerState.Inactive)
    val timerState: StateFlow<SleepTimerState> = _timerState.asStateFlow()

    private val _fadeOutEnabled = MutableStateFlow(true)

    private var onTimerFinished: (() -> Unit)? = null
    private var onFadeVolume: ((Float) -> Unit)? = null

    fun setCallbacks(onFinish: () -> Unit, onFade: (Float) -> Unit) {
        this.onTimerFinished = onFinish
        this.onFadeVolume = onFade
    }

    fun startTimer(durationMinutes: Int, endOfTrack: Boolean = false, fadeOut: Boolean = _fadeOutEnabled.value) {
        cancelTimer()
        val totalMs = durationMinutes * 60 * 1000L

        timerJob = scope.launch {
            var remaining = totalMs
            _timerState.value = SleepTimerState.Active(remaining, totalMs, endOfTrack, fadeOut)

            while (remaining > 0) {
                delay(1000L)
                remaining -= 1000L

                // If fade out is enabled, gradually reduce volume over the last 30 seconds
                if (fadeOut && remaining <= 30_000L) {
                    val fadeFactor = (remaining.toFloat() / 30_000f).coerceIn(0.05f, 1f)
                    onFadeVolume?.invoke(fadeFactor)
                }

                _timerState.value = SleepTimerState.Active(remaining, totalMs, endOfTrack, fadeOut)
            }

            _timerState.value = SleepTimerState.Inactive
            onTimerFinished?.invoke()
        }
    }

    fun startEndOfTrackTimer() {
        cancelTimer()
        _timerState.value = SleepTimerState.Active(
            remainingMs = 0L,
            totalMs = 0L,
            endOfTrack = true,
            fadeOut = false
        )
    }

    fun onTrackEnded() {
        val current = _timerState.value
        if (current is SleepTimerState.Active && current.endOfTrack) {
            cancelTimer()
            onTimerFinished?.invoke()
        }
    }

    fun cancelTimer() {
        timerJob?.cancel()
        timerJob = null
        _timerState.value = SleepTimerState.Inactive
        onFadeVolume?.invoke(1.0f)
    }

    val remainingMillis: StateFlow<Long> = timerState.map { (it as? SleepTimerState.Active)?.remainingMs ?: 0L }
        .stateIn(scope, SharingStarted.Eagerly, 0L)

    val isTimerActive: StateFlow<Boolean> = timerState.map { it is SleepTimerState.Active }
        .stateIn(scope, SharingStarted.Eagerly, false)

    val isEndOfTrack: StateFlow<Boolean> = timerState.map { (it as? SleepTimerState.Active)?.endOfTrack ?: false }
        .stateIn(scope, SharingStarted.Eagerly, false)

    val fadeOutEnabled: StateFlow<Boolean> = _fadeOutEnabled.asStateFlow()

    fun setStopAtEndOfTrack(enabled: Boolean) {
        if (enabled) {
            startEndOfTrackTimer()
        } else {
            val current = _timerState.value
            if (current is SleepTimerState.Active && current.endOfTrack) {
                cancelTimer()
            }
        }
    }

    fun setFadeOutEnabled(enabled: Boolean) {
        _fadeOutEnabled.value = enabled
    }
}
