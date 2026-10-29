package com.songsync.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.songsync.app.navigation.SongSyncNavGraph
import com.songsync.core.domain.repository.SettingsRepository
import com.songsync.core.ui.theme.SongSyncTheme
import com.songsync.feature.player.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
            val settings by settingsRepository.settingsFlow.collectAsState(
                initial = com.songsync.core.domain.model.UserSettings()
            )

            SongSyncTheme(
                themeMode = settings.themeMode,
                palette = settings.palette,
                useDynamicColor = settings.useDynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val scope = rememberCoroutineScope()
                    SongSyncNavGraph(
                        isOnboardingCompleted = settings.onboardingCompleted,
                        onOnboardingCompleted = {
                            scope.launch { settingsRepository.setOnboardingCompleted(true) }
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
