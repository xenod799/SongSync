package com.songsync.core.domain.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val songCount: Int,
    val year: Int,
    val artworkUri: Uri?
)
