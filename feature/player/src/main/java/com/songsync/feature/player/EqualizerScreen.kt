package com.songsync.feature.player

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.theme.PillShape
import com.songsync.media.player.EqualizerManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    equalizerManager: EqualizerManager,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by equalizerManager.state.collectAsState()
    var presetMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Equalizer", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { equalizerManager.setEnabled(it) },
                        modifier = Modifier.padding(end = 12.dp)
                    )
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
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Preset Selector
            if (state.presets.isNotEmpty()) {
                Text(
                    text = "Preset",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                val currentPresetName = if (state.currentPresetIndex in state.presets.indices) {
                    state.presets[state.currentPresetIndex.toInt()]
                } else "Custom"

                ExposedDropdownMenuBox(
                    expanded = presetMenuExpanded,
                    onExpandedChange = { presetMenuExpanded = !presetMenuExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = currentPresetName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetMenuExpanded) },
                        shape = PillShape,
                        enabled = state.isEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = presetMenuExpanded,
                        onDismissRequest = { presetMenuExpanded = false }
                    ) {
                        state.presets.forEachIndexed { index, name ->
                            DropdownMenuItem(
                                text = { Text(name) },
                                onClick = {
                                    equalizerManager.setPreset(index.toShort())
                                    presetMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // EQ Frequency Bands
            Text(
                text = "Frequency Bands",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            state.bands.forEach { band ->
                val minDb = band.minLevelMilliBels / 100
                val maxDb = band.maxLevelMilliBels / 100
                val currentDb = band.currentLevelMilliBels / 100

                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (band.centerFreqHz >= 1000) "${band.centerFreqHz / 1000} kHz" else "${band.centerFreqHz} Hz",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${if (currentDb > 0) "+$currentDb" else "$currentDb"} dB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = band.currentLevelMilliBels.toFloat(),
                        onValueChange = {
                            equalizerManager.setBandLevel(band.index, it.toInt().toShort())
                        },
                        valueRange = band.minLevelMilliBels.toFloat()..band.maxLevelMilliBels.toFloat(),
                        enabled = state.isEnabled
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // Audio Effects: Bass Boost & Virtualizer & Loudness
            Text(
                text = "Audio Enhancement",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Bass Boost
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Bass Boost", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${(state.bassBoostStrength / 10)}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = state.bassBoostStrength.toFloat(),
                    onValueChange = { equalizerManager.setBassBoost(it.toInt().toShort()) },
                    valueRange = 0f..1000f,
                    enabled = state.isEnabled
                )
            }

            // Virtualizer
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Virtualizer (Surround)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${(state.virtualizerStrength / 10)}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = state.virtualizerStrength.toFloat(),
                    onValueChange = { equalizerManager.setVirtualizer(it.toInt().toShort()) },
                    valueRange = 0f..1000f,
                    enabled = state.isEnabled
                )
            }

            // Loudness Enhancer
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Loudness Enhancer", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("${state.loudnessGainMilliBels / 100} dB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = state.loudnessGainMilliBels.toFloat(),
                    onValueChange = { equalizerManager.setLoudness(it.toInt()) },
                    valueRange = 0f..1000f,
                    enabled = state.isEnabled
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
