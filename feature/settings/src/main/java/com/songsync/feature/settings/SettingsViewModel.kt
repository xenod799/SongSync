package com.songsync.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.songsync.core.domain.model.ArtShape
import com.songsync.core.domain.model.ColorPalette
import com.songsync.core.domain.model.ThemeMode
import com.songsync.core.domain.model.UserSettings
import com.songsync.core.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UserSettings()
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        settingsRepository.setThemeMode(mode)
    }

    fun setPalette(palette: ColorPalette) = viewModelScope.launch {
        settingsRepository.setColorPalette(palette)
    }

    fun setDynamicColor(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setUseDynamicColor(enabled)
    }

    fun setArtShape(shape: ArtShape) = viewModelScope.launch {
        settingsRepository.setArtShape(shape)
    }

    fun setAnimateArtPulse(animate: Boolean) = viewModelScope.launch {
        settingsRepository.setAnimateArtPulse(animate)
    }

    fun setMinDurationSeconds(seconds: Int) = viewModelScope.launch {
        settingsRepository.setMinTrackDurationSeconds(seconds)
    }

    fun setCrossfadeDurationSeconds(seconds: Int) = viewModelScope.launch {
        settingsRepository.setCrossfadeDurationSeconds(seconds)
    }

    fun setGaplessEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setGaplessEnabled(enabled)
    }

    fun setReplayGainEnabled(enabled: Boolean) = viewModelScope.launch {
        settingsRepository.setReplayGainEnabled(enabled)
    }

    fun setOnboardingCompleted(completed: Boolean) = viewModelScope.launch {
        settingsRepository.setOnboardingCompleted(completed)
    }

    suspend fun exportBackupJson(): String = settingsRepository.exportBackupJson()

    suspend fun restoreBackupJson(jsonString: String): Boolean = settingsRepository.restoreBackupJson(jsonString)
}
