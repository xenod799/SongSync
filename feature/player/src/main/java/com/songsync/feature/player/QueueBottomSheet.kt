package com.songsync.feature.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.songsync.core.domain.model.Playlist
import com.songsync.core.domain.model.Song
import com.songsync.core.ui.theme.BottomSheetTopShape
import com.songsync.core.ui.theme.ListItemShape
import com.songsync.core.ui.theme.PillShape
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val QueueRowHeight = 64.dp

/** One row of the queue. Keys stay attached to a song while it is dragged. */
private data class QueueEntry(val key: String, val song: Song)

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun QueueBottomSheet(
    queue: List<Song>,
    currentIndex: Int,
    playlists: List<Playlist>,
    onSongSelected: (Int) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onInsertIntoQueue: (Song, Int) -> Unit,
    onReorderQueue: (Int, Int) -> Unit,
    onAddSongsToPlaylist: (Long, List<Long>) -> Unit,
    onSaveAsPlaylist: (String) -> Unit,
    onClearQueue: () -> Unit,
    onDismiss: () -> Unit
) {
    // Always open fully; the sheet height is capped by its content below.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var menuExpanded by remember { mutableStateOf(false) }
    var showPlaylistPicker by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    // Long-press context target for a single queue row.
    var contextSong by remember { mutableStateOf<Song?>(null) }
    // When non-null, the playlist picker adds these songs (single or whole queue).
    var playlistTargetSongs by remember { mutableStateOf<List<Song>?>(null) }

    // Stable keys keep row state attached to the song rather than to its position.
    // The occurrence suffix makes duplicate songs in the queue unambiguous.
    val entries = remember(queue) {
        val occurrences = HashMap<Long, Int>()
        queue.map { song ->
            val occurrence = occurrences.getOrDefault(song.id, 0)
            occurrences[song.id] = occurrence + 1
            QueueEntry(key = "${song.id}_$occurrence", song = song)
        }
    }

    val rowHeightPx = with(LocalDensity.current) { QueueRowHeight.toPx() }
    var draggedKey by remember { mutableStateOf<String?>(null) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    // Reordering rebuilds the queue list, so read the newest values from the
    // gesture callbacks instead of capturing composition-time snapshots.
    val latestEntries by rememberUpdatedState(entries)
    val latestQueueSize by rememberUpdatedState(queue.size)
    val latestOnReorder by rememberUpdatedState(onReorderQueue)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetTopShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 560.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Playing Queue (${queue.size})",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                contentDescription = "Queue options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add to playlist") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.PlaylistAdd, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    playlistTargetSongs = queue.toList()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Save as new playlist") },
                                leadingIcon = {
                                    Icon(Icons.Rounded.BookmarkBorder, contentDescription = null)
                                },
                                onClick = {
                                    menuExpanded = false
                                    showSaveDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear queue") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.ClearAll,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onClearQueue()
                                }
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (queue.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Your queue is empty",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 16.dp)
                    ) {
                        itemsIndexed(
                            items = entries,
                            key = { _, entry -> entry.key }
                        ) { index, entry ->
                            val song = entry.song
                            val isCurrent = index == currentIndex
                            val isDragging = entry.key == draggedKey

                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                        onRemoveFromQueue(index)
                                        scope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Removed ${song.title}",
                                                actionLabel = "Undo"
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                onInsertIntoQueue(song, index)
                                            }
                                        }
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(ListItemShape)
                                            .background(MaterialTheme.colorScheme.errorContainer)
                                            .padding(end = 20.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            ) {
                                // The row background must be fully opaque: a translucent
                                // highlight would let the red delete background bleed through.
                                val rowColor = if (isCurrent) {
                                    lerp(
                                        MaterialTheme.colorScheme.surface,
                                        MaterialTheme.colorScheme.primaryContainer,
                                        0.55f
                                    )
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(QueueRowHeight)
                                        .offset {
                                            IntOffset(
                                                x = 0,
                                                y = if (isDragging) dragOffsetPx.roundToInt() else 0
                                            )
                                        }
                                        .zIndex(if (isDragging) 1f else 0f)
                                        .clip(ListItemShape)
                                        .background(rowColor)
                                        .combinedClickable(
                                            onClick = { onSongSelected(index) },
                                            onLongClick = { contextSong = song }
                                        )
                                        .padding(start = 12.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Rounded.Equalizer,
                                            contentDescription = "Currently playing",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = song.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isCurrent) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = song.artistName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .pointerInput(Unit) {
                                                detectDragGestures(
                                                    onDragStart = {
                                                        draggedKey = entry.key
                                                        dragOffsetPx = 0f
                                                    },
                                                    onDrag = { change, dragAmount ->
                                                        change.consume()
                                                        dragOffsetPx += dragAmount.y

                                                        val steps =
                                                            (dragOffsetPx / rowHeightPx).roundToInt()
                                                        if (steps != 0) {
                                                            val from = latestEntries
                                                                .indexOfFirst { it.key == draggedKey }
                                                            if (from >= 0) {
                                                                val to = (from + steps).coerceIn(
                                                                    0,
                                                                    latestQueueSize - 1
                                                                )
                                                                if (to != from) {
                                                                    latestOnReorder(from, to)
                                                                    dragOffsetPx -= steps * rowHeightPx
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onDragEnd = {
                                                        draggedKey = null
                                                        dragOffsetPx = 0f
                                                    },
                                                    onDragCancel = {
                                                        draggedKey = null
                                                        dragOffsetPx = 0f
                                                    }
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DragHandle,
                                            contentDescription = "Drag to reorder",
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }

    val targetSongs = playlistTargetSongs
    if (targetSongs != null) {
        AlertDialog(
            onDismissRequest = { playlistTargetSongs = null },
            title = { Text(if (targetSongs.size == 1) "Add Song to Playlist" else "Add to Playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists yet. Create one to add these songs.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(playlists, key = { it.id }) { playlist ->
                            ListItem(
                                headlineContent = { Text(playlist.name) },
                                supportingContent = { Text("${playlist.trackCount} songs") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddSongsToPlaylist(playlist.id, targetSongs.map { it.id })
                                        playlistTargetSongs = null
                                    }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        playlistTargetSongs = null
                        showSaveDialog = true
                    }
                ) {
                    Text(if (playlists.isEmpty()) "Create playlist" else "New playlist")
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistTargetSongs = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Queue as Playlist") },
            text = {
                OutlinedTextField(
                    value = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label = { Text("Playlist name") },
                    singleLine = true,
                    shape = PillShape,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            onSaveAsPlaylist(newPlaylistName)
                            showSaveDialog = false
                            newPlaylistName = ""
                        }
                    },
                    shape = PillShape
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Per-song context actions (long-press on a queue row).
    val ctx = contextSong
    if (ctx != null) {
        AlertDialog(
            onDismissRequest = { contextSong = null },
            title = { Text(ctx.title) },
            text = {
                Column {
                    ListItem(
                        headlineContent = { Text("Add to playlist") },
                        leadingContent = { Icon(Icons.Rounded.PlaylistAdd, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                contextSong = null
                                playlistTargetSongs = listOf(ctx)
                            }
                    )
                    ListItem(
                        headlineContent = { Text("Remove from queue", color = MaterialTheme.colorScheme.error) },
                        leadingContent = {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val idx = queue.indexOfFirst { it === ctx || it.id == ctx.id && it == ctx }
                                // best-effort: find by identity then by first matching id
                                val realIdx = queue.indexOf(ctx).let { i ->
                                    if (i >= 0) i else queue.indexOfFirst { it.id == ctx.id }
                                }
                                if (realIdx >= 0) onRemoveFromQueue(realIdx)
                                contextSong = null
                            }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { contextSong = null }) { Text("Cancel") }
            }
        )
    }
}
