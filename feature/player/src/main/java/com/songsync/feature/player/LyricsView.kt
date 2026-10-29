package com.songsync.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.songsync.core.domain.model.Lyrics
import com.songsync.core.ui.theme.BottomSheetTopShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsBottomSheet(
    lyrics: Lyrics?,
    currentPositionMs: Long,
    onSeekTo: (Long) -> Unit,
    onAdjustOffset: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = BottomSheetTopShape,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Header with Sync Offset Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Lyrics",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (lyrics != null && lyrics.isSynced) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Offset: ${lyrics.offsetMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        IconButton(onClick = { onAdjustOffset(-500L) }) {
                            Icon(imageVector = Icons.Rounded.Remove, contentDescription = "-0.5s")
                        }
                        IconButton(onClick = { onAdjustOffset(500L) }) {
                            Icon(imageVector = Icons.Rounded.Add, contentDescription = "+0.5s")
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (lyrics == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No lyrics found for this track",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (lyrics.isSynced) {
                // Time-Synced Lyrics with smooth auto-scroll
                val listState = rememberLazyListState()
                val adjustedPos = currentPositionMs + lyrics.offsetMs

                val activeLineIndex = remember(adjustedPos, lyrics.lines) {
                    lyrics.lines.indexOfLast { it.timestampMs <= adjustedPos }.coerceAtLeast(0)
                }

                LaunchedEffect(activeLineIndex) {
                    if (activeLineIndex >= 0) {
                        listState.animateScrollToItem((activeLineIndex - 2).coerceAtLeast(0))
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 24.dp)
                ) {
                    itemsIndexed(lyrics.lines) { index, line ->
                        val isActive = index == activeLineIndex
                        Text(
                            text = line.text,
                            fontSize = if (isActive) 22.sp else 18.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeekTo(line.timestampMs) }
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            } else {
                // Plain-text Fallback
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 24.dp)
                ) {
                    Text(
                        text = lyrics.plainText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 28.sp
                    )
                }
            }
        }
    }
}
