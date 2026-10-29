package com.songsync.core.domain.model

enum class SortBy {
    TITLE,
    ARTIST,
    ALBUM,
    DATE_ADDED,
    DURATION,
    PLAY_COUNT
}

data class SortOrder(
    val sortBy: SortBy = SortBy.TITLE,
    val isAscending: Boolean = true
)
