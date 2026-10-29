package com.songsync.core.data.repository

import android.media.MediaMetadataRetriever
import com.songsync.core.data.local.db.dao.LyricsDao
import com.songsync.core.data.local.db.entity.LyricsOffsetEntity
import com.songsync.core.domain.model.LyricLine
import com.songsync.core.domain.model.Lyrics
import com.songsync.core.domain.repository.LyricsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.File
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LyricsRepositoryImpl @Inject constructor(
    private val lyricsDao: LyricsDao
) : LyricsRepository {

    private val lrcTimestampPattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\]")

    override fun getLyrics(songId: Long, songPath: String): Flow<Lyrics?> = flow {
        val userOffset = lyricsDao.getOffsetSync(songId) ?: 0L

        // 1. Check sidecar .lrc file first
        val lrcFile = File(songPath.substringBeforeLast(".") + ".lrc")
        if (lrcFile.exists() && lrcFile.canRead()) {
            val content = try { lrcFile.readText() } catch (_: Exception) { "" }
            if (content.isNotBlank()) {
                val parsed = parseLrc(songId, content, userOffset)
                emit(parsed)
                return@flow
            }
        }

        // 2. Extract embedded lyrics from audio metadata
        val embeddedText = extractEmbeddedLyrics(songPath)
        if (!embeddedText.isNullOrBlank()) {
            val parsed = parseLrc(songId, embeddedText, userOffset)
            emit(parsed)
            return@flow
        }

        emit(null)
    }.flowOn(Dispatchers.IO)

    override suspend fun saveLyricsOffset(songId: Long, offsetMs: Long) {
        lyricsDao.saveOffset(LyricsOffsetEntity(songId = songId, offsetMs = offsetMs))
    }

    private fun parseLrc(songId: Long, rawContent: String, userOffsetMs: Long): Lyrics {
        val lines = mutableListOf<LyricLine>()
        var offsetFromHeader = 0L

        rawContent.lines().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("[offset:", ignoreCase = true)) {
                val offsetStr = trimmed.substringAfter(":").substringBefore("]").trim()
                offsetFromHeader = offsetStr.toLongOrNull() ?: 0L
                return@forEach
            }

            val matcher = lrcTimestampPattern.matcher(trimmed)
            if (matcher.find()) {
                val minutes = matcher.group(1)?.toLongOrNull() ?: 0L
                val seconds = matcher.group(2)?.toLongOrNull() ?: 0L
                val msPart = matcher.group(3) ?: "0"
                val millis = if (msPart.length == 2) msPart.toLong() * 10 else msPart.toLong()
                val timestamp = minutes * 60_000L + seconds * 1_000L + millis
                val lyricText = trimmed.substring(matcher.end()).trim()
                lines.add(LyricLine(timestampMs = timestamp, text = lyricText))
            }
        }

        val totalOffset = userOffsetMs + offsetFromHeader
        val isSynced = lines.isNotEmpty()

        return if (isSynced) {
            lines.sortBy { it.timestampMs }
            Lyrics(
                songId = songId,
                isSynced = true,
                lines = lines,
                plainText = lines.joinToString("\n") { it.text },
                offsetMs = totalOffset
            )
        } else {
            Lyrics(
                songId = songId,
                isSynced = false,
                lines = emptyList(),
                plainText = rawContent,
                offsetMs = 0L
            )
        }
    }

    private fun extractEmbeddedLyrics(songPath: String): String? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(songPath)
            // Some devices or tags store lyrics in metadata keys
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_COMPILATION)
        } catch (_: Exception) {
            null
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }
}
