package com.songsync.feature.player

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Forward10
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay10
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.ui.component.FavoriteHeartButton
import com.songsync.core.ui.component.ExpressiveMusicNotePlaceholder
import com.songsync.core.ui.component.MorphingPlayPauseButton
import com.songsync.core.ui.component.WavySlider
import com.songsync.core.ui.theme.Cookie9SidedShape
import com.songsync.core.ui.theme.PlayerArtDefaultShape
import com.songsync.core.ui.theme.SoftBurstShape
import com.songsync.core.ui.theme.isAnimationDisabled

@Composable
fun NowPlayingScreen(
    viewModel: PlayerViewModel,
    onCollapse: () -> Unit,
    onOpenQueue: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenInfo: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenSpeedPitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playbackState by viewModel.playerConnection.playbackState.collectAsState()
    val artworkColorHex by viewModel.artworkColor.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val currentSong = playbackState.currentSong ?: return
    val context = LocalContext.current
    val noAnim = isAnimationDisabled()

    // Artwork pulse animation: 1.0 to 1.02 over 6s reverse loop
    val infiniteTransition = rememberInfiniteTransition(label = "artPulse")
    val artScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "artScalePulse"
    )
    val appliedArtScale = if (playbackState.isPlaying && settings.animateArtPulse && !noAnim) artScale else 1.0f

    // Selected Art Shape
    val artShape = when (settings.artShape) {
        ArtShape.ROUNDED_SQUARE -> PlayerArtDefaultShape
        ArtShape.CIRCLE -> CircleShape
        ArtShape.COOKIE -> Cookie9SidedShape()
        ArtShape.SOFT_BURST -> SoftBurstShape()
    }

    // Two-stop vertical gradient over top 45% using artwork-derived color or active primaryContainer
    val gradientTopColor = if (artworkColorHex != null) {
        Color(artworkColorHex!!).copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
    }
    val backgroundBrush = Brush.verticalGradient(
        0.0f to gradientTopColor,
        0.45f to MaterialTheme.colorScheme.surface,
        1.0f to MaterialTheme.colorScheme.surface
    )

    // Gesture handling: swipe down to collapse, swipe left/right for prev/next
    var dragAccumulatedX by remember { mutableStateOf(0f) }
    var dragAccumulatedY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .background(backgroundBrush)
            .systemBarsPadding()
            .displayCutoutPadding()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        if (dragAccumulatedY > 150f) {
                            onCollapse()
                        } else if (dragAccumulatedX < -150f) {
                            viewModel.playerConnection.seekToNext()
                        } else if (dragAccumulatedX > 150f) {
                            viewModel.playerConnection.seekToPrevious()
                        }
                        dragAccumulatedX = 0f
                        dragAccumulatedY = 0f
                    },
                    onDragCancel = {
                        dragAccumulatedX = 0f
                        dragAccumulatedY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragAccumulatedX += dragAmount.x
                        dragAccumulatedY += dragAmount.y
                    }
                )
            }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Collapse down arrow, Song Info, Queue shortcuts
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = onCollapse,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Collapse player",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    IconButton(onClick = onOpenInfo) {
                        Icon(imageVector = Icons.Rounded.Info, contentDescription = "Song Info")
                    }
                    IconButton(onClick = onOpenQueue) {
                        Icon(imageVector = Icons.Rounded.QueueMusic, contentDescription = "Queue")
                    }
                }
            }

            // Large Album Artwork — flexes to fit any display height, centered in leftover space
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .aspectRatio(1f)
                        .scale(appliedArtScale)
                        .clip(artShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentSong.albumArtUri != null) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(currentSong.albumArtUri)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        ExpressiveMusicNotePlaceholder(
                            modifier = Modifier.fillMaxSize(),
                            iconSize = 100.dp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Song Title & Artist + Favorite Heart Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentSong.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${currentSong.artistName} • ${currentSong.albumName}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                FavoriteHeartButton(
                    isFavorite = currentSong.isFavorite,
                    onToggleFavorite = { viewModel.toggleFavorite(currentSong) },
                    size = 48.dp,
                    iconSize = 28.dp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wavy Seek Bar (amplitude animates 6dp playing, 0dp paused, wavelength 28dp)
            val sliderProgress = if (playbackState.durationMs > 0) {
                (playbackState.currentPositionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Column(modifier = Modifier.fillMaxWidth()) {
                WavySlider(
                    value = sliderProgress,
                    onValueChange = { fraction ->
                        val targetMs = (fraction * playbackState.durationMs).toLong()
                        viewModel.playerConnection.seekTo(targetMs)
                    },
                    isPlaying = playbackState.isPlaying
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(playbackState.currentPositionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTime(playbackState.durationMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Main Playback Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Mode Toggle
                IconButton(onClick = { viewModel.playerConnection.setShuffleMode(!playbackState.shuffleModeEnabled) }) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playbackState.shuffleModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Skip -10s
                IconButton(onClick = { viewModel.playerConnection.skipBackward10s() }) {
                    Icon(imageVector = Icons.Rounded.Replay10, contentDescription = "Skip back 10s", modifier = Modifier.size(28.dp))
                }

                // Previous Track
                IconButton(onClick = { viewModel.playerConnection.seekToPrevious() }) {
                    Icon(imageVector = Icons.Rounded.SkipPrevious, contentDescription = "Previous Track", modifier = Modifier.size(34.dp))
                }

                // Morphing Play/Pause Button (72dp, Cookie9Sided <-> Circle)
                MorphingPlayPauseButton(
                    isPlaying = playbackState.isPlaying,
                    onClick = { viewModel.playerConnection.togglePlayPause() },
                    size = 72.dp
                )

                // Next Track
                IconButton(onClick = { viewModel.playerConnection.seekToNext() }) {
                    Icon(imageVector = Icons.Rounded.SkipNext, contentDescription = "Next Track", modifier = Modifier.size(34.dp))
                }

                // Skip +10s
                IconButton(onClick = { viewModel.playerConnection.skipForward10s() }) {
                    Icon(imageVector = Icons.Rounded.Forward10, contentDescription = "Skip forward 10s", modifier = Modifier.size(28.dp))
                }

                // Repeat Mode Toggle
                IconButton(onClick = { viewModel.playerConnection.toggleRepeatMode() }) {
                    val repeatIcon = if (playbackState.repeatMode == 1) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat
                    val repeatTint = if (playbackState.repeatMode != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    Icon(imageVector = repeatIcon, contentDescription = "Repeat", tint = repeatTint)
                }
            }

            // Bottom action shortcuts: Speed/Pitch and Lyrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenSpeedPitch) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = "Playback Speed",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onOpenLyrics) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = "Lyrics",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onOpenSleepTimer) {
                    Icon(
                        imageVector = Icons.Rounded.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
