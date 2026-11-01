package com.songsync.core.data.metadata

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import javax.inject.Inject

data class ArtistProfile(
    val name: String,
    val bio: String,
    val imageUrl: String?
)

class WikipediaClient @Inject constructor() {

    fun fetchArtistProfile(artistName: String): ArtistProfile? {
        return try {
            val title = URLEncoder.encode(artistName.replace(' ', '_'), "UTF-8")
            val url = "https://en.wikipedia.org/api/rest_v1/page/summary/$title"
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
            val displayTitle = json.optString("title").takeIf { it.isNotBlank() } ?: artistName
            val extract = json.optString("extract").takeIf { it.isNotBlank() } ?: ""
            val thumbnail = json.optJSONObject("thumbnail")
            val imageUrl = thumbnail?.optString("source")?.takeIf { it.isNotBlank() }

            ArtistProfile(name = displayTitle, bio = extract, imageUrl = imageUrl)
        } catch (_: Exception) {
            null
        }
    }
}
