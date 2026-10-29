package com.songsync.feature.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.songsync.core.domain.model.Song
import com.songsync.core.ui.theme.BottomSheetTopShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongInfoBottomSheet(
    song: Song,
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
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Track Information",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            val fileSizeMb = String.format("%.2f MB", song.sizeBytes / (1024.0 * 1024.0))
            val bitrateKbps = if (song.bitrate > 0) "${song.bitrate / 1000} kbps" else "Unknown"
            val sampleRateKhz = if (song.sampleRate > 0) "${song.sampleRate / 1000.0} kHz" else "44.1 kHz"

            InfoRow("Title", song.title)
            InfoRow("Artist", song.artistName)
            InfoRow("Album", song.albumName)
            InfoRow("Genre", song.genre ?: "Unknown")
            InfoRow("Year", if (song.year > 0) song.year.toString() else "Unknown")
            InfoRow("Format", song.mimeType.substringAfterLast("/"))
            InfoRow("Bitrate", bitrateKbps)
            InfoRow("Sample Rate", sampleRateKhz)
            InfoRow("File Size", fileSizeMb)
            InfoRow("File Path", song.dataPath)

            if (song.replayGainTrackGainDb != null) {
                InfoRow("ReplayGain Track", "${song.replayGainTrackGainDb} dB")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.65f)
        )
    }
}
