package com.songsync.feature.library.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.songsync.core.domain.model.Song
import com.songsync.core.ui.component.EmptyStateView
import com.songsync.core.ui.component.FastScroller
import com.songsync.core.ui.component.FavoriteHeartButton
import com.songsync.core.ui.theme.ListArtShape
import com.songsync.core.ui.theme.ListItemShape
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongsTab(
    songs: List<Song>,
    selectedIds: Set<Long>,
    isMultiSelectMode: Boolean,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    onToggleFavorite: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) {
        EmptyStateView(
            title = "No songs found",
            subtitle = "Your audio library is empty. Add songs to your device storage to start listening.",
            modifier = modifier
        )
        return
    }

    val listState = rememberLazyListState()
    val songTitles = remember(songs) { songs.map { it.title } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(end = 36.dp)
        ) {
            itemsIndexed(
                items = songs,
                key = { _, song -> song.id }
            ) { index, song ->
                // Staggered entrance capped at first 12 items
                var isVisible by remember { mutableStateOf(index >= 12) }
                LaunchedEffect(Unit) {
                    if (index < 12) {
                        delay(index * 40L)
                        isVisible = true
                    }
                }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 16 })
                ) {
                    SongListItem(
                        song = song,
                        isSelected = selectedIds.contains(song.id),
                        isMultiSelectMode = isMultiSelectMode,
                        onClick = {
                            if (isMultiSelectMode) {
                                onSongLongClick(song)
                            } else {
                                onSongClick(song)
                            }
                        },
                        onLongClick = { onSongLongClick(song) },
                        onToggleFavorite = { onToggleFavorite(song) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(96.dp)) // Padding for mini player & floating nav
            }
        }

        FastScroller(
            items = songTitles,
            listState = listState,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongListItem(
    song: Song,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val containerBg = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ListItemShape)
            .background(containerBg)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Multi-select Checkbox or Album Art
        if (isMultiSelectMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        // 16dp rounded square album artwork
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(ListArtShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (song.albumArtUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song.albumArtUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title and Artist / Duration
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${song.artistName} • ${formatDuration(song.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Favorite heart button with radiating butter sparkles
        FavoriteHeartButton(
            isFavorite = song.isFavorite,
            onToggleFavorite = onToggleFavorite,
            size = 38.dp,
            iconSize = 22.dp
        )
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
