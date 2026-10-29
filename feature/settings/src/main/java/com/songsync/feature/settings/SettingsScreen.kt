package com.songsync.feature.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.ColorPalette
import com.songsync.core.domain.model.ThemeMode
import com.songsync.core.ui.theme.CardContainerShape
import com.songsync.core.ui.theme.PillShape
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onOpenEqualizer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    var backupJsonDialog by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreInputText by remember { mutableStateOf("") }
    var showAboutDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Section: Appearance & Theme
            SectionHeader(icon = Icons.Rounded.ColorLens, title = "Appearance")

            Text("Theme Mode", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    val isSelected = settings.themeMode == mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Palette Picker
            Text("Color Palette", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            val paletteSeeds = listOf(
                ColorPalette.PEACH_POP to Color(0xFFF0668A),
                ColorPalette.MINT_SODA to Color(0xFF2BAE8E),
                ColorPalette.LAVENDER_DREAM to Color(0xFF8C7BFF),
                ColorPalette.BUBBLEGUM to Color(0xFFFF5FA2),
                ColorPalette.SUNNY_DAY to Color(0xFFE89A00)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                paletteSeeds.forEach { (palette, color) ->
                    val isSelected = settings.palette == palette
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 0.dp,
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = CircleShape
                            )
                            .clickable { viewModel.setPalette(palette) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(imageVector = Icons.Rounded.Check, contentDescription = null, tint = Color.White)
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dynamic Color (Material You)", style = MaterialTheme.typography.bodyLarge)
                        Text("Tint interface with system wallpaper colors", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = settings.useDynamicColor,
                        onCheckedChange = viewModel::setDynamicColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Section: Playback
            SectionHeader(icon = Icons.Rounded.GraphicEq, title = "Playback")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardContainerShape)
                    .clickable(onClick = onOpenEqualizer)
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Equalizer & Audio Effects", style = MaterialTheme.typography.bodyLarge)
                    Text("Bass boost, virtualizer, loudness enhancer", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Gapless Playback", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = settings.isGaplessEnabled, onCheckedChange = viewModel::setGaplessEnabled)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ReplayGain Normalization", style = MaterialTheme.typography.bodyLarge)
                    Text("Equalize track volume when tags exist", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = settings.isReplayGainEnabled, onCheckedChange = viewModel::setReplayGainEnabled)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text("Crossfade Duration: ${settings.crossfadeDurationSeconds}s", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = settings.crossfadeDurationSeconds.toFloat(),
                onValueChange = { viewModel.setCrossfadeDurationSeconds(it.toInt()) },
                valueRange = 0f..12f,
                steps = 11
            )

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Section: Library
            SectionHeader(icon = Icons.Rounded.LibraryMusic, title = "Library")

            Text("Minimum Track Duration: ${settings.minTrackDurationSeconds}s", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = settings.minTrackDurationSeconds.toFloat(),
                onValueChange = { viewModel.setMinDurationSeconds(it.toInt()) },
                valueRange = 0f..120f,
                steps = 23
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Album Art Shape", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ArtShape.entries.forEach { shape ->
                    val isSelected = settings.artShape == shape
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(PillShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { viewModel.setArtShape(shape) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = shape.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pulse Album Art While Playing", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = settings.animateArtPulse, onCheckedChange = viewModel::setAnimateArtPulse)
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Section: Backup & Restore
            SectionHeader(icon = Icons.Rounded.Backup, title = "Data & Backup")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val json = viewModel.exportBackupJson()
                            backupJsonDialog = json
                        }
                    },
                    shape = PillShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export JSON")
                }

                Button(
                    onClick = { showRestoreDialog = true },
                    shape = PillShape,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Restore JSON")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // Section: About
            SectionHeader(icon = Icons.Rounded.Info, title = "About")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAboutDialog = true }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("SongSync v1.0.0", style = MaterialTheme.typography.titleMedium)
                    Text("Fully offline, zero bloat, native Android music player", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Export Backup JSON Dialog
    if (backupJsonDialog != null) {
        AlertDialog(
            onDismissRequest = { backupJsonDialog = null },
            title = { Text("Backup JSON") },
            text = {
                OutlinedTextField(
                    value = backupJsonDialog ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            },
            confirmButton = {
                Button(onClick = { backupJsonDialog = null }, shape = PillShape) { Text("Close") }
            }
        )
    }

    // Restore JSON Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Paste Backup JSON") },
            text = {
                OutlinedTextField(
                    value = restoreInputText,
                    onValueChange = { restoreInputText = it },
                    placeholder = { Text("Paste your exported JSON backup here") },
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.restoreBackupJson(restoreInputText)
                            showRestoreDialog = false
                            restoreInputText = ""
                        }
                    },
                    shape = PillShape
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About SongSync") },
            text = {
                Text(
                    "SongSync is an expressive, fully offline music player crafted with Jetpack Compose, Material 3 Expressive, and Android Media3.\n\n• Zero ads, tracking, or network permissions\n• 100% offline device library\n• ReplayGain, crossfade, and equalizer\n• Apache 2.0 open source license"
                )
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }, shape = PillShape) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        modifier = Modifier.padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
