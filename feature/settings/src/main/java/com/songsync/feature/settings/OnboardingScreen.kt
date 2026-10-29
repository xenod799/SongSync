package com.songsync.feature.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.songsync.core.ui.mascot.SyncMascot
import com.songsync.core.ui.mascot.SyncPose
import com.songsync.core.ui.theme.PillShape

@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableIntStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onOnboardingFinished()
    }

    val steps = listOf(
        OnboardingStep(
            title = "Meet Sync, your music buddy",
            description = "Welcome to SongSync! A modern, playful music player created purely for offline listening with zero ads or tracking.",
            pose = SyncPose.HAPPY
        ),
        OnboardingStep(
            title = "Expressive & Colorful",
            description = "Customize your listening experience with 5 vibrant palettes, AMOLED mode, wavy seekbars, and tactile morphing animations.",
            pose = SyncPose.HAPPY
        ),
        OnboardingStep(
            title = "100% Offline & Private",
            description = "SongSync doesn't even have internet access permission. Your audio library, playlists, and listening history never leave your device.",
            pose = SyncPose.SEARCHING
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip Button
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (currentStep < steps.size - 1) {
                TextButton(onClick = { currentStep = steps.size - 1 }) {
                    Text("Skip", color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }
        }

        // Mascot & Step Content
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "onboardingStep"
        ) { stepIndex ->
            val step = steps[stepIndex]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                SyncMascot(
                    pose = step.pose,
                    size = 180.dp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Navigation controls and permission request
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.indices.forEach { index ->
                    val isSelected = index == currentStep
                    Box(
                        modifier = Modifier
                            .size(if (isSelected) 24.dp else 8.dp, 8.dp)
                            .clip(PillShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (currentStep < steps.size - 1) {
                        currentStep++
                    } else {
                        // Request audio permissions
                        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            arrayOf(
                                Manifest.permission.READ_MEDIA_AUDIO,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        permissionLauncher.launch(perms)
                    }
                },
                shape = PillShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Text(
                    text = if (currentStep < steps.size - 1) "Next" else "Grant Storage Access",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private data class OnboardingStep(
    val title: String,
    val description: String,
    val pose: SyncPose
)
