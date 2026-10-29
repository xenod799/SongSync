package com.songsync.media.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.songsync.core.data.local.db.dao.SongDao
import com.songsync.core.data.mapper.toDomain
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.media.player.EqualizerManager
import com.songsync.media.player.ReplayGainHelper
import com.songsync.media.player.SleepTimerManager
import com.songsync.media.session.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SongSyncMediaService : MediaLibraryService() {

    @Inject lateinit var songDao: SongDao
    @Inject lateinit var settingsRepository: SettingsRepository
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var equalizerManager: EqualizerManager
    @Inject lateinit var sleepTimerManager: SleepTimerManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaLibrarySession: MediaLibrarySession

    private var currentSong: Song? = null
    private var isReplayGainEnabled = true

    companion object {
        const val ROOT_ID = "root"
        const val SONGS_ID = "root_songs"
        const val ALBUMS_ID = "root_albums"
        const val ARTISTS_ID = "root_artists"
        const val PLAYLISTS_ID = "root_playlists"
        const val ACTION_OPEN_PLAYER = "com.songsync.OPEN_PLAYER"
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeSession()
        setupSleepTimer()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // Handles audio focus automatically
            .setHandleAudioBecomingNoisy(true) // Pauses on headphone unplug or Bluetooth disconnect
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        equalizerManager.bindAudioSession(exoPlayer.audioSessionId)

        serviceScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                isReplayGainEnabled = settings.isReplayGainEnabled
                exoPlayer.playbackParameters = PlaybackParameters(settings.playbackSpeed, settings.playbackPitch)
                applyReplayGain(currentSong)
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val mediaId = mediaItem?.mediaId?.toLongOrNull() ?: return
                serviceScope.launch {
                    val songEntity = songDao.getSongByIdSync(mediaId)
                    val song = songEntity?.toDomain()
                    currentSong = song
                    applyReplayGain(song)

                    // Record playback and update session
                    songDao.incrementPlayCount(mediaId, System.currentTimeMillis())
                    saveCurrentSession()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    sleepTimerManager.onTrackEnded()
                }
                saveCurrentSession()
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                saveCurrentSession()
            }
        })
    }

    private fun initializeSession() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            action = ACTION_OPEN_PLAYER
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaLibrarySession = MediaLibrarySession.Builder(this, exoPlayer, LibrarySessionCallback())
            .setSessionActivity(pendingIntent)
            .build()
    }

    private fun setupSleepTimer() {
        sleepTimerManager.setCallbacks(
            onFinish = {
                exoPlayer.pause()
            },
            onFade = { fadeFactor ->
                val baseVolume = ReplayGainHelper.calculateVolume(
                    currentSong?.replayGainTrackGainDb,
                    currentSong?.replayGainTrackPeak,
                    isReplayGainEnabled
                )
                exoPlayer.volume = baseVolume * fadeFactor
            }
        )
    }

    private fun applyReplayGain(song: Song?) {
        val volume = ReplayGainHelper.calculateVolume(
            song?.replayGainTrackGainDb,
            song?.replayGainTrackPeak,
            isReplayGainEnabled
        )
        exoPlayer.volume = volume
    }

    private fun saveCurrentSession() {
        val song = currentSong ?: return
        val currentPos = exoPlayer.currentPosition
        val queueIds = (0 until exoPlayer.mediaItemCount).mapNotNull {
            exoPlayer.getMediaItemAt(it).mediaId.toLongOrNull()
        }
        serviceScope.launch {
            sessionManager.saveSession(
                songId = song.id,
                positionMs = currentPos,
                queueSongIds = queueIds,
                shuffleMode = exoPlayer.shuffleModeEnabled,
                repeatMode = exoPlayer.repeatMode
            )
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        saveCurrentSession()
        equalizerManager.release()
        serviceScope.cancel()
        mediaLibrarySession.release()
        exoPlayer.release()
        super.onDestroy()
    }

    private inner class LibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId(ROOT_ID)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setTitle("SongSync Library")
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return when (parentId) {
                ROOT_ID -> {
                    val categories = listOf(
                        createBrowsableItem(SONGS_ID, "Songs"),
                        createBrowsableItem(ALBUMS_ID, "Albums"),
                        createBrowsableItem(ARTISTS_ID, "Artists"),
                        createBrowsableItem(PLAYLISTS_ID, "Playlists")
                    )
                    Futures.immediateFuture(LibraryResult.ofItemList(categories, params))
                }
                SONGS_ID -> {
                    val future = com.google.common.util.concurrent.SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()
                    serviceScope.launch(Dispatchers.IO) {
                        val songs = songDao.getAllSongsList()
                        val mediaItems = songs.map { song ->
                            MediaItem.Builder()
                                .setMediaId(song.id.toString())
                                .setUri(song.contentUriString)
                                .setMediaMetadata(
                                    MediaMetadata.Builder()
                                        .setTitle(song.title)
                                        .setArtist(song.artistName)
                                        .setAlbumTitle(song.albumName)
                                        .setArtworkUri(song.albumArtUriString?.let { Uri.parse(it) })
                                        .setIsPlayable(true)
                                        .setIsBrowsable(false)
                                        .build()
                                )
                                .build()
                        }
                        future.set(LibraryResult.ofItemList(mediaItems, params))
                    }
                    future
                }
                else -> Futures.immediateFuture(LibraryResult.ofItemList(emptyList(), params))
            }
        }

        private fun createBrowsableItem(id: String, title: String): MediaItem {
            return MediaItem.Builder()
                .setMediaId(id)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .build()
                )
                .build()
        }
    }
}
