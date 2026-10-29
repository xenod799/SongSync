package com.songsync.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.songsync.app.MainActivity

class SongSyncWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(150.dp, 60.dp),
            DpSize(250.dp, 120.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .cornerRadius(20.dp)
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .cornerRadius(10.dp)
                            .background(GlanceTheme.colors.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♪",
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.width(10.dp))

                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "SongSync",
                            maxLines = 1,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Tap to open library",
                            maxLines = 1,
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(8.dp))

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WidgetControlButton(text = "⏮", action = PlaybackActionCallback.ACTION_PREVIOUS)
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    WidgetControlButton(text = "⏯", action = PlaybackActionCallback.ACTION_PLAY_PAUSE)
                    Spacer(modifier = GlanceModifier.width(12.dp))
                    WidgetControlButton(text = "⏭", action = PlaybackActionCallback.ACTION_NEXT)
                }
            }
        }
    }

    @Composable
    private fun WidgetControlButton(text: String, action: String) {
        Box(
            modifier = GlanceModifier
                .size(36.dp)
                .cornerRadius(18.dp)
                .background(GlanceTheme.colors.secondaryContainer)
                .clickable(
                    actionRunCallback<PlaybackActionCallback>(
                        actionParametersOf(PlaybackActionCallback.KEY_ACTION to action)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    color = GlanceTheme.colors.onSecondaryContainer,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

class PlaybackActionCallback : ActionCallback {
    companion object {
        val KEY_ACTION = ActionParameters.Key<String>("playback_action")
        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREVIOUS = "action_previous"
    }

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val action = parameters[KEY_ACTION] ?: return
        val serviceIntent = Intent("com.songsync.media.ACTION_WIDGET_CONTROL").apply {
            setPackage(context.packageName)
            putExtra("widget_action", action)
        }
        try {
            context.startService(serviceIntent)
        } catch (_: Exception) {}
    }
}
