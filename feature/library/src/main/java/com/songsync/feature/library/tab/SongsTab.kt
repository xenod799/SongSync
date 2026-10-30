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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.songsync.core.ui.component.ExpressiveMusicNotePlaceholder
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
    onEditTags: ((Song) -> Unit)? = null,
    hasPermission: Boolean = true,
    onRequestPermission: () -> Unit = {},
    onScanClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (songs.isEmpty()) {
        if (!hasPermission) {
            EmptyStateView(
                title = "Permission Required",
                subtitle = "SongSync needs access to your audio files so it can discover and play your local music offline.",
                actionButtonText = "Grant Storage Access",
                onActionClick = onRequestPermission,
                modifier = modifier
            )
        } else {
            EmptyStateView(
                title = "No songs found",
                subtitle = "Your audio library is empty. Scan your device to discover downloaded music files.",
                actionButtonText = "Scan for Music",
                onActionClick = onScanClick,
                modifier = modifier
            )
        }
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
                        onEditTags = onEditTags?.let { callback -> { callback(song) } },
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
    onEditTags: (() -> Unit)? = null,
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
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                ExpressiveMusicNotePlaceholder(modifier = Modifier.fillMaxSize())
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

        // Favorite heart in a 48dp cell so its touch/center aligns with the
        // options cell below (heart itself is 38dp, centered in the slot)
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            FavoriteHeartButton(
                isFavorite = song.isFavorite,
                onToggleFavorite = onToggleFavorite,
                size = 38.dp,
                iconSize = 22.dp
            )
        }

        if (onEditTags != null && !isMultiSelectMode) {
            var menuExpanded by remember { mutableStateOf(false) }
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "More options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit tags") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onEditTags()
                        }
                    )
                }
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
