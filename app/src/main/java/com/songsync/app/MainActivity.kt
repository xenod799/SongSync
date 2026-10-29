package com.songsync.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.songsync.app.navigation.SongSyncNavGraph
import com.songsync.core.domain.model.AppThemeMode
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.ui.theme.SongSyncPalette
import com.songsync.core.ui.theme.SongSyncTheme
import com.songsync.feature.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleAudioIntent(intent)

        setContent {
            val settings by settingsRepository.userSettings.collectAsState(
                initial = com.songsync.core.domain.model.UserSettings()
            )

            val isDark = when (settings.themeMode) {
                AppThemeMode.SYSTEM -> isSystemInDarkTheme()
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK, AppThemeMode.AMOLED -> true
            }

            val isAmoled = settings.themeMode == AppThemeMode.AMOLED

            val palette = try {
                SongSyncPalette.valueOf(settings.colorPalette)
            } catch (_: Exception) {
                SongSyncPalette.PEACH_POP
            }

            SongSyncTheme(
                palette = palette,
                darkTheme = isDark,
                amoled = isAmoled,
                dynamicColor = settings.useDynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SongSyncNavGraph(
                        isOnboardingCompleted = settings.isOnboardingCompleted,
                        onOnboardingCompleted = {
                            settingsRepository.setOnboardingCompleted(true)
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAudioIntent(intent)
    }

    private fun handleAudioIntent(intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        val uri: Uri? = if (action == Intent.ACTION_VIEW) {
            intent.data
        } else if (action == Intent.ACTION_SEND) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM)
        } else null

        if (uri != null) {
            playerViewModel.playUri(uri)
        }
    }
}
