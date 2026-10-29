package com.songsync.media.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "songsync_session")

data class PlaybackSession(
    val songId: Long,
    val positionMs: Long,
    val queueSongIds: List<Long>,
    val shuffleMode: Boolean,
    val repeatMode: Int
)

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_SONG_ID = longPreferencesKey("last_song_id")
        val LAST_POSITION_MS = longPreferencesKey("last_position_ms")
        val QUEUE_IDS_JSON = stringPreferencesKey("queue_ids_json")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
    }

    suspend fun saveSession(
        songId: Long,
        positionMs: Long,
        queueSongIds: List<Long>,
        shuffleMode: Boolean,
        repeatMode: Int
    ) {
        context.sessionDataStore.edit { prefs ->
            prefs[Keys.LAST_SONG_ID] = songId
            prefs[Keys.LAST_POSITION_MS] = positionMs
            prefs[Keys.QUEUE_IDS_JSON] = Json.encodeToString(queueSongIds)
            prefs[Keys.SHUFFLE_MODE] = shuffleMode
            prefs[Keys.REPEAT_MODE] = repeatMode
        }
    }

    suspend fun restoreSession(): PlaybackSession? {
        val prefs = context.sessionDataStore.data.first()
        val songId = prefs[Keys.LAST_SONG_ID] ?: return null
        val positionMs = prefs[Keys.LAST_POSITION_MS] ?: 0L
        val queueJson = prefs[Keys.QUEUE_IDS_JSON]
        val queueIds = if (!queueJson.isNullOrBlank()) {
            try { Json.decodeFromString<List<Long>>(queueJson) } catch (_: Exception) { emptyList() }
        } else {
            emptyList()
        }
        val shuffleMode = prefs[Keys.SHUFFLE_MODE] ?: false
        val repeatMode = prefs[Keys.REPEAT_MODE] ?: 0

        return PlaybackSession(
            songId = songId,
            positionMs = positionMs,
            queueSongIds = queueIds,
            shuffleMode = shuffleMode,
            repeatMode = repeatMode
        )
    }
}
