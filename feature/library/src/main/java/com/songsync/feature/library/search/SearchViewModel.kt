package com.songsync.feature.library.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songsync.core.domain.model.Song
import com.songsync.core.domain.repository.SongRepository
import com.songsync.media.player.PlayerConnection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val searchResults: List<Song> = emptyList(),
    val recentSearches: List<String> = emptyList()
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val songRepository: SongRepository,
    val playerConnection: PlayerConnection
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _recentSearches = MutableStateFlow<List<String>>(emptyList())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val searchResults: StateFlow<List<Song>> = _query
        .debounce(200L)
        .flatMapLatest { q ->
            if (q.isBlank()) flowOf(emptyList()) else songRepository.search(q.trim())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun submitSearch(term: String) {
        _query.value = term
        if (term.isNotBlank()) {
            _recentSearches.update { list ->
                (listOf(term) + (list - term)).take(10)
            }
        }
    }

    fun removeRecentSearch(term: String) {
        _recentSearches.update { it - term }
    }

    fun clearRecentSearches() {
        _recentSearches.value = emptyList()
    }

    fun playSong(song: Song) {
        val list = searchResults.value
        playerConnection.playSong(song, list.ifEmpty { listOf(song) })
    }
}
