package com.songsync.core.data.metadata

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

data class MbAlbumMetadata(
    val title: String,
    val artist: String,
    val year: Int,
    val coverArtUrl: String?
)

class MusicBrainzClient @Inject constructor() {

    fun searchRelease(artist: String, album: String): MbAlbumMetadata? {
        return try {
            val query = "artist:\"${artist}\" AND release:\"${album}\""
            val url = "https://musicbrainz.org/ws/2/release/?query=${URLEncoder.encode(query, "UTF-8")}&fmt=json&limit=1"
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "SongSync/1.0 (https://github.com/xenod799/SongSync)")
                setRequestProperty("Accept", "application/json")
                connectTimeout = 15000
                readTimeout = 15000
            }
            if (conn.responseCode != 200) {
                conn.disconnect()
                return null
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()

            val json = JSONObject(body)
            val releases = json.optJSONArray("releases") ?: return null
            if (releases.length() == 0) return null
            val rel = releases.getJSONObject(0)
            val title = rel.optString("title").takeIf { it.isNotBlank() } ?: album
            val artistCredit = rel.optJSONArray("artist-credit")
            val artistName = if (artistCredit != null && artistCredit.length() > 0) {
                artistCredit.getJSONObject(0).optString("name").takeIf { it.isNotBlank() } ?: artist
            } else artist
            val date = rel.optString("date")
            val year = date.take(4).toIntOrNull() ?: 0
            val mbid = rel.optString("id")
            val coverUrl = if (mbid.isNotBlank()) {
                "https://coverartarchive.org/release/$mbid/front-250"
            } else null

            MbAlbumMetadata(title = title, artist = artistName, year = year, coverArtUrl = coverUrl)
        } catch (_: Exception) {
            null
        }
    }
}
