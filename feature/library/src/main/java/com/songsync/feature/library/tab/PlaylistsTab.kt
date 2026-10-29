package com.songsync.feature.library.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.songsync.core.domain.model.Playlist
import com.songsync.core.ui.component.EmptyStateView
import com.songsync.core.ui.theme.CardContainerShape
import com.songsync.core.ui.theme.ListArtShape
import com.songsync.core.ui.theme.PillShape

@Composable
fun PlaylistsTab(
    playlists: List<Playlist>,
    onPlaylistClick: (Playlist) -> Unit,
    onCreatePlaylist: () -> Unit,
    onRenamePlaylist: (Playlist) -> Unit,
    onDeletePlaylist: (Playlist) -> Unit,
    onDuplicatePlaylist: (Playlist) -> Unit,
    onExportM3u: (Playlist) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (playlists.isEmpty()) {
            EmptyStateView(
                title = "No playlists yet",
                subtitle = "Create custom playlists to organize your favorite music completely offline.",
                actionButtonText = "Create playlist",
                onActionClick = onCreatePlaylist,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp)
            ) {
                items(playlists, key = { it.id }) { playlist ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardContainerShape)
                            .clickable { onPlaylistClick(playlist) }
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Playlist Icon / Auto Cover Mosaic
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(ListArtShape)
                                .background(MaterialTheme.colorScheme.tertiaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QueueMusic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${playlist.trackCount} songs • ${formatDuration(playlist.totalDurationMs)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // More options dropdown
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Rounded.MoreVert,
                                    contentDescription = "Options",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename") },
                                    onClick = {
                                        menuExpanded = false
                                        onRenamePlaylist(playlist)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Duplicate") },
                                    onClick = {
                                        menuExpanded = false
                                        onDuplicatePlaylist(playlist)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export M3U") },
                                    onClick = {
                                        menuExpanded = false
                                        onExportM3u(playlist)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        onDeletePlaylist(playlist)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to create playlist
        FloatingActionButton(
            onClick = onCreatePlaylist,
            shape = PillShape,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 100.dp)
        ) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Create Playlist")
        }
    }
}
