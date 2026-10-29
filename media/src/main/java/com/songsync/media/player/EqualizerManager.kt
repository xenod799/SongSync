package com.songsync.media.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.os.Build
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class EqualizerBand(
    val index: Short,
    val centerFreqHz: Int,
    val minLevelMilliBels: Short,
    val maxLevelMilliBels: Short,
    val currentLevelMilliBels: Short
)

data class EqualizerState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPresetIndex: Short = -1,
    val bassBoostStrength: Short = 0,
    val virtualizerStrength: Short = 0,
    val loudnessGainMilliBels: Int = 0
)

@Singleton
class EqualizerManager @Inject constructor() {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null

    private val _state = MutableStateFlow(EqualizerState())
    val state: StateFlow<EqualizerState> = _state.asStateFlow()

    fun bindAudioSession(audioSessionId: Int) {
        release()
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                    enabled = _state.value.isEnabled
                }
            }

            readCurrentAudioFxState()
        } catch (_: Exception) {
            // AudioSession may not support some effects on specific vendor ROMs
        }
    }

    private fun readCurrentAudioFxState() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands
        val minLevel = eq.bandLevelRange[0]
        val maxLevel = eq.bandLevelRange[1]

        val bands = (0 until numBands).map { i ->
            val shortIdx = i.toShort()
            EqualizerBand(
                index = shortIdx,
                centerFreqHz = eq.getCenterFreq(shortIdx) / 1000,
                minLevelMilliBels = minLevel,
                maxLevelMilliBels = maxLevel,
                currentLevelMilliBels = eq.getBandLevel(shortIdx)
            )
        }

        val numPresets = eq.numberOfPresets
        val presetNames = (0 until numPresets).map { eq.getPresetName(it.toShort()) }

        _state.value = _state.value.copy(
            bands = bands,
            presets = presetNames,
            currentPresetIndex = eq.currentPreset
        )
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        bassBoost?.enabled = enabled
        virtualizer?.enabled = enabled
        loudnessEnhancer?.enabled = enabled
        _state.value = _state.value.copy(isEnabled = enabled)
    }

    fun setBandLevel(bandIndex: Short, levelMilliBels: Short) {
        val eq = equalizer ?: return
        eq.setBandLevel(bandIndex, levelMilliBels)
        val updatedBands = _state.value.bands.map {
            if (it.index == bandIndex) it.copy(currentLevelMilliBels = levelMilliBels) else it
        }
        _state.value = _state.value.copy(bands = updatedBands, currentPresetIndex = -1)
    }

    fun setPreset(presetIndex: Short) {
        val eq = equalizer ?: return
        eq.usePreset(presetIndex)
        readCurrentAudioFxState()
    }

    fun setBassBoost(strength: Short) {
        bassBoost?.let {
            it.setStrength(strength)
            _state.value = _state.value.copy(bassBoostStrength = strength)
        }
    }

    fun setVirtualizer(strength: Short) {
        virtualizer?.let {
            it.setStrength(strength)
            _state.value = _state.value.copy(virtualizerStrength = strength)
        }
    }

    fun setLoudness(gainMilliBels: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            loudnessEnhancer?.let {
                it.setTargetGain(gainMilliBels)
                _state.value = _state.value.copy(loudnessGainMilliBels = gainMilliBels)
            }
        }
    }

    fun release() {
        try { equalizer?.release() } catch (_: Exception) {}
        try { bassBoost?.release() } catch (_: Exception) {}
        try { virtualizer?.release() } catch (_: Exception) {}
        try { loudnessEnhancer?.release() } catch (_: Exception) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        loudnessEnhancer = null
    }
}
