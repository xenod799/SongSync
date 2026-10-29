package com.songsync.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import com.songsync.core.domain.model.ColorPalette
import com.songsync.core.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongSyncTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    palette: ColorPalette = ColorPalette.PEACH_POP,
    useDynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDark = isSystemInDarkTheme()

    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
    }

    val isAmoled = themeMode == ThemeMode.AMOLED

    val colorScheme: ColorScheme = when {
        useDynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamic = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            if (isAmoled) createAmoledScheme(dynamic) else dynamic
        }
        else -> {
            val base = when (palette) {
                ColorPalette.PEACH_POP -> if (isDark) PeachPopDark else PeachPopLight
                ColorPalette.MINT_SODA -> if (isDark) MintSodaDark else MintSodaLight
                ColorPalette.LAVENDER_DREAM -> if (isDark) LavenderDreamDark else LavenderDreamLight
                ColorPalette.BUBBLEGUM -> if (isDark) BubblegumDark else BubblegumLight
                ColorPalette.SUNNY_DAY -> if (isDark) SunnyDayDark else SunnyDayLight
            }
            if (isAmoled) createAmoledScheme(base) else base
        }
    }

    val extras = if (isDark) SongSyncExtrasDark else SongSyncExtrasLight

    CompositionLocalProvider(
        LocalSongSyncExtras provides extras
    ) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            shapes = SongSyncShapes,
            typography = SongSyncTypography,
            motionScheme = MotionScheme.expressive(),
            content = content
        )
    }
}
