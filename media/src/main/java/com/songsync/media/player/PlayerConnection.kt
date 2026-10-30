package com.songsync.media.player

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.mapper.toDomain
import com.songsync.core.domain.model.Song
import com.songsync.media.service.SongSyncMediaService
import com.songsync.media.session.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerConnection @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val sessionManager: SessionManager,
    private val sleepTimerManager: SleepTimerManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var positionUpdateJob: Job? = null

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private var currentQueueSongs = mutableListOf<Song>()

    init {
        connectToService()
        observeSleepTimer()
    }

    private fun connectToService() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, SongSyncMediaService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener(
            {
                mediaController = controllerFuture?.get()
                setupControllerListener()
                updateStateFromController()
                scope.launch { restoreLastSession() }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    private fun observeSleepTimer() {
        scope.launch {
            sleepTimerManager.timerState.collect { timerState ->
                _playbackState.update { it.copy(sleepTimerState = timerState) }
            }
        }
    }

    private fun setupControllerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                scope.launch {
                    val mediaId = mediaItem?.mediaId?.toLongOrNull()
                    val song = mediaId?.let { songDao.getSongByIdSync(it)?.toDomain() }
                    val currentIndex = mediaController?.currentMediaItemIndex ?: -1
                    _playbackState.update {
                        it.copy(
                            currentSong = song,
                            currentQueueIndex = currentIndex,
                            durationMs = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                        )
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _playbackState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) {
                    startPositionUpdates()
                } else {
                    stopPositionUpdates()
                }
            }

            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
                _playbackState.update {
                    it.copy(
                        playbackSpeed = playbackParameters.speed,
                        playbackPitch = playbackParameters.pitch
                    )
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _playbackState.update { it.copy(repeatMode = repeatMode) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _playbackState.update { it.copy(shuffleModeEnabled = shuffleModeEnabled) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                _playbackState.update { it.copy(durationMs = duration) }
            }
        })
    }

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = scope.launch {
            while (isActive) {
                val current = mediaController?.currentPosition ?: 0L
                val buffered = mediaController?.bufferedPosition ?: 0L
                val duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L
                _playbackState.update {
                    it.copy(
                        currentPositionMs = current,
                        bufferedPositionMs = buffered,
                        durationMs = if (duration > 0) duration else it.durationMs
                    )
                }
                delay(100L) // 100ms smooth seekbar tracking
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        val current = mediaController?.currentPosition ?: 0L
        _playbackState.update { it.copy(currentPositionMs = current) }
    }

    private fun updateStateFromController() {
        val controller = mediaController ?: return
        val currentMediaId = controller.currentMediaItem?.mediaId?.toLongOrNull()
        scope.launch {
            val song = currentMediaId?.let { songDao.getSongByIdSync(it)?.toDomain() }
            _playbackState.update {
                it.copy(
                    currentSong = song,
                    isPlaying = controller.isPlaying,
                    currentPositionMs = controller.currentPosition,
                    durationMs = controller.duration.coerceAtLeast(0L),
                    bufferedPositionMs = controller.bufferedPosition,
                    shuffleModeEnabled = controller.shuffleModeEnabled,
                    repeatMode = controller.repeatMode,
                    playbackSpeed = controller.playbackParameters.speed,
                    playbackPitch = controller.playbackParameters.pitch,
                    currentQueueIndex = controller.currentMediaItemIndex
                )
            }
            // The controller may attach while the session is already playing
            // (e.g. after a session restore), in which case onIsPlayingChanged
            // never fires — start position updates explicitly or the seekbar
            // stays frozen at the restored position.
            if (controller.isPlaying) {
                startPositionUpdates()
            }
        }
    }

    suspend fun restoreLastSession() {
        val session = sessionManager.restoreSession() ?: return
        if (session.queueSongIds.isEmpty()) return

        val songEntities = songDao.getAllSongsList().associateBy { it.id }
        val restoredQueue = session.queueSongIds.mapNotNull { songEntities[it]?.toDomain() }
        if (restoredQueue.isEmpty()) return

        val targetIndex = restoredQueue.indexOfFirst { it.id == session.songId }.coerceAtLeast(0)
        setQueue(restoredQueue, targetIndex, startPlayback = false, initialPositionMs = session.positionMs)

        mediaController?.shuffleModeEnabled = session.shuffleMode
        mediaController?.repeatMode = session.repeatMode
    }

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        setQueue(queue, index, startPlayback = true)
    }

    fun setQueue(queue: List<Song>, startIndex: Int, startPlayback: Boolean, initialPositionMs: Long = 0L) {
        val controller = mediaController ?: return
        currentQueueSongs = queue.toMutableList()

        val mediaItems = queue.map { it.toMediaItem() }
        controller.setMediaItems(mediaItems, startIndex, initialPositionMs)
        controller.prepare()

        if (startPlayback) {
            controller.play()
        }

        _playbackState.update {
            it.copy(
                queue = queue,
                currentQueueIndex = startIndex,
                currentSong = queue.getOrNull(startIndex)
            )
        }
    }

    fun playNext(song: Song) {
        val controller = mediaController ?: return
        val currentIndex = controller.currentMediaItemIndex
        val insertIndex = (currentIndex + 1).coerceAtMost(currentQueueSongs.size)

        currentQueueSongs.add(insertIndex, song)
        controller.addMediaItem(insertIndex, song.toMediaItem())

        _playbackState.update { it.copy(queue = currentQueueSongs.toList()) }
    }

    fun addToQueue(song: Song) {
        val controller = mediaController ?: return
        currentQueueSongs.add(song)
        controller.addMediaItem(song.toMediaItem())

        _playbackState.update { it.copy(queue = currentQueueSongs.toList()) }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _playbackState.update { it.copy(currentPositionMs = positionMs) }
    }

    fun skipForward10s() {
        val controller = mediaController ?: return
        val position = controller.currentPosition.coerceAtLeast(0L)
        // duration is C.TIME_UNSET (a large negative) for unknown/live media —
        // coercing against it would produce a negative garbage position
        val duration = controller.duration
        val newPos = if (duration == C.TIME_UNSET || duration <= 0L) {
            position + 10_000L
        } else {
            (position + 10_000L).coerceAtMost(duration)
        }
        seekTo(newPos)
    }

    fun skipBackward10s() {
        val controller = mediaController ?: return
        val newPos = (controller.currentPosition - 10_000L).coerceAtLeast(0L)
        seekTo(newPos)
    }

    fun seekToNext() {
        mediaController?.seekToNextMediaItem()
    }

    fun skipToNext() {
        seekToNext()
    }

    fun seekToPrevious() {
        val controller = mediaController ?: return
        // If track played > 3 seconds, seek to beginning of current track first
        if (controller.currentPosition > 3000L) {
            controller.seekTo(0L)
        } else {
            controller.seekToPreviousMediaItem()
        }
    }

    fun skipToPrevious() {
        seekToPrevious()
    }

    fun setShuffleMode(enabled: Boolean) {
        mediaController?.shuffleModeEnabled = enabled
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun toggleRepeatMode() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
    }

    fun cycleRepeatMode() {
        toggleRepeatMode()
    }

    fun playUri(uri: Uri) {
        val controller = mediaController ?: return
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(uri.lastPathSegment ?: "Audio Track")
                    .build()
            )
            .build()
        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
    }

    fun setPlaybackSpeed(speed: Float) {
        val controller = mediaController ?: return
        val pitch = controller.playbackParameters.pitch
        controller.playbackParameters = PlaybackParameters(speed, pitch)
    }

    fun setPlaybackPitch(pitch: Float) {
        val controller = mediaController ?: return
        val speed = controller.playbackParameters.speed
        controller.playbackParameters = PlaybackParameters(speed, pitch)
    }

    fun setPlaybackParameters(speed: Float, pitch: Float) {
        mediaController?.playbackParameters = PlaybackParameters(speed, pitch)
    }

    fun reorderQueue(fromIndex: Int, toIndex: Int) {
        val controller = mediaController ?: return
        if (fromIndex in currentQueueSongs.indices && toIndex in currentQueueSongs.indices) {
            val item = currentQueueSongs.removeAt(fromIndex)
            currentQueueSongs.add(toIndex, item)
            controller.moveMediaItem(fromIndex, toIndex)
            _playbackState.update {
                it.copy(
                    queue = currentQueueSongs.toList(),
                    currentQueueIndex = controller.currentMediaItemIndex
                )
            }
        }
    }

    fun removeFromQueue(index: Int) {
        val controller = mediaController ?: return
        if (index in currentQueueSongs.indices) {
            currentQueueSongs.removeAt(index)
            controller.removeMediaItem(index)
            _playbackState.update {
                it.copy(
                    queue = currentQueueSongs.toList(),
                    currentQueueIndex = controller.currentMediaItemIndex
                )
            }
        }
    }

    fun clearQueue() {
        val controller = mediaController ?: return
        currentQueueSongs.clear()
        controller.clearMediaItems()
        _playbackState.update {
            it.copy(
                queue = emptyList(),
                currentQueueIndex = -1,
                currentSong = null
            )
        }
    }

    private fun Song.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(contentUri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artistName)
                    .setAlbumTitle(albumName)
                    .setArtworkUri(albumArtUri)
                    .build()
            )
            .build()
    }
}
