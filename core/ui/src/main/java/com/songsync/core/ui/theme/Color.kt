package com.songsync.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.songsync.core.domain.model.ColorPalette

// Peach Pop (Default)
val PeachPopLight = lightColorScheme(
    primary = Color(0xFFF0668A),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3F0019),
    secondary = Color(0xFF7B6CF0),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5DEFF),
    onSecondaryContainer = Color(0xFF1B0A66),
    tertiary = Color(0xFF1FA67A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFBDF5DC),
    onTertiaryContainer = Color(0xFF002115),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFFFF8F5),
    onBackground = Color(0xFF2A1F22),
    surface = Color(0xFFFFF8F5),
    onSurface = Color(0xFF2A1F22),
    surfaceVariant = Color(0xFFF6DDE3),
    onSurfaceVariant = Color(0xFF5A4147),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFFF1EC),
    surfaceContainer = Color(0xFFFDEBE5),
    surfaceContainerHigh = Color(0xFFF8E5DF),
    surfaceContainerHighest = Color(0xFFF2DFD9),
    outline = Color(0xFF8C7077),
    outlineVariant = Color(0xFFDFC0C7),
    inverseSurface = Color(0xFF3F3034),
    inverseOnSurface = Color(0xFFFFEDF0),
    inversePrimary = Color(0xFFFFB1C4),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFF0668A)
)

val PeachPopDark = darkColorScheme(
    primary = Color(0xFFFFB1C4),
    onPrimary = Color(0xFF650B2F),
    primaryContainer = Color(0xFF8A2A4B),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFC9BFFF),
    onSecondary = Color(0xFF2F1F8F),
    secondaryContainer = Color(0xFF463AA8),
    onSecondaryContainer = Color(0xFFE5DEFF),
    tertiary = Color(0xFF7FDDB8),
    onTertiary = Color(0xFF00382A),
    tertiaryContainer = Color(0xFF00513C),
    onTertiaryContainer = Color(0xFFBDF5DC),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1E1519),
    onBackground = Color(0xFFF3DDE2),
    surface = Color(0xFF1E1519),
    onSurface = Color(0xFFF3DDE2),
    surfaceVariant = Color(0xFF5A4147),
    onSurfaceVariant = Color(0xFFDFC0C7),
    surfaceContainerLowest = Color(0xFF180F13),
    surfaceContainerLow = Color(0xFF261A1F),
    surfaceContainer = Color(0xFF2B1E23),
    surfaceContainerHigh = Color(0xFF36282D),
    surfaceContainerHighest = Color(0xFF413238),
    outline = Color(0xFFA68A91),
    outlineVariant = Color(0xFF5A4147),
    inverseSurface = Color(0xFFF3DDE2),
    inverseOnSurface = Color(0xFF3F3034),
    inversePrimary = Color(0xFFF0668A),
    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFB1C4)
)

fun createAmoledScheme(baseDark: ColorScheme): ColorScheme = baseDark.copy(
    background = Color(0xFF000000),
    surface = Color(0xFF000000),
    surfaceContainerLowest = Color(0xFF000000),
    surfaceContainerLow = Color(0xFF0D0709),
    surfaceContainer = Color(0xFF130B0E),
    surfaceContainerHigh = Color(0xFF1A1015),
    surfaceContainerHighest = Color(0xFF21151A)
)

// Mint Soda Palette
val MintSodaLight = lightColorScheme(
    primary = Color(0xFF2BAE8E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA6F2D6),
    onPrimaryContainer = Color(0xFF002117),
    secondary = Color(0xFF5AA9F0),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E7FF),
    onSecondaryContainer = Color(0xFF001C38),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDDB8),
    onTertiaryContainer = Color(0xFF2B1700),
    background = Color(0xFFF4FFFA),
    onBackground = Color(0xFF141F1A),
    surface = Color(0xFFF4FFFA),
    onSurface = Color(0xFF141F1A),
    surfaceContainer = Color(0xFFE2F4EC),
    surfaceContainerHigh = Color(0xFFDAEFE6),
    surfaceContainerHighest = Color(0xFFD0E9DF)
)

val MintSodaDark = darkColorScheme(
    primary = Color(0xFF87D7BC),
    onPrimary = Color(0xFF003828),
    primaryContainer = Color(0xFF00513B),
    onPrimaryContainer = Color(0xFFA6F2D6),
    secondary = Color(0xFFA4CDFF),
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF00497B),
    onSecondaryContainer = Color(0xFFD4E7FF),
    tertiary = Color(0xFFFFB86B),
    onTertiary = Color(0xFF482900),
    background = Color(0xFF0E1A15),
    onBackground = Color(0xFFDEEBE4),
    surface = Color(0xFF0E1A15),
    onSurface = Color(0xFFDEEBE4),
    surfaceContainer = Color(0xFF162520),
    surfaceContainerHigh = Color(0xFF1E302A),
    surfaceContainerHighest = Color(0xFF263C34)
)

// Lavender Dream Palette
val LavenderDreamLight = lightColorScheme(
    primary = Color(0xFF8C7BFF),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE7E0FF),
    onPrimaryContainer = Color(0xFF1E0062),
    secondary = Color(0xFFFF9CCB),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD8E7),
    onSecondaryContainer = Color(0xFF3B0024),
    tertiary = Color(0xFF7FD8E8),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF7FF),
    onBackground = Color(0xFF1D1A23),
    surface = Color(0xFFFAF7FF),
    onSurface = Color(0xFF1D1A23),
    surfaceContainer = Color(0xFFEDE7F6),
    surfaceContainerHigh = Color(0xFFE5DEEF),
    surfaceContainerHighest = Color(0xFFDCD4E7)
)

val LavenderDreamDark = darkColorScheme(
    primary = Color(0xFFCBC3FF),
    onPrimary = Color(0xFF2B0B8C),
    primaryContainer = Color(0xFF4328A6),
    onPrimaryContainer = Color(0xFFE7E0FF),
    secondary = Color(0xFFFFB0D5),
    onSecondary = Color(0xFF5A003B),
    tertiary = Color(0xFF83D7E8),
    onTertiary = Color(0xFF00363F),
    background = Color(0xFF16121D),
    onBackground = Color(0xFFE6E0EB),
    surface = Color(0xFF16121D),
    onSurface = Color(0xFFE6E0EB),
    surfaceContainer = Color(0xFF221C2A),
    surfaceContainerHigh = Color(0xFF2D2637),
    surfaceContainerHighest = Color(0xFF383144)
)

// Bubblegum Palette
val BubblegumLight = lightColorScheme(
    primary = Color(0xFFFF5FA2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E6),
    onPrimaryContainer = Color(0xFF3E001F),
    secondary = Color(0xFF5EC8FF),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCBE9FF),
    onSecondaryContainer = Color(0xFF001F2F),
    tertiary = Color(0xFFFFD84D),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFF5FA),
    onBackground = Color(0xFF24191E),
    surface = Color(0xFFFFF5FA),
    onSurface = Color(0xFF24191E),
    surfaceContainer = Color(0xFFFCE6EF),
    surfaceContainerHigh = Color(0xFFF6DBE6),
    surfaceContainerHighest = Color(0xFFEFCFDD)
)

val BubblegumDark = darkColorScheme(
    primary = Color(0xFFFFB0D0),
    onPrimary = Color(0xFF5F0034),
    primaryContainer = Color(0xFF860D4C),
    onPrimaryContainer = Color(0xFFFFD9E6),
    secondary = Color(0xFF86D3FF),
    onSecondary = Color(0xFF00354E),
    tertiary = Color(0xFFFFE082),
    onTertiary = Color(0xFF402D00),
    background = Color(0xFF1E1217),
    onBackground = Color(0xFFF3DEE5),
    surface = Color(0xFF1E1217),
    onSurface = Color(0xFFF3DEE5),
    surfaceContainer = Color(0xFF2A1B22),
    surfaceContainerHigh = Color(0xFF36242C),
    surfaceContainerHighest = Color(0xFF422D37)
)

// Sunny Day Palette
val SunnyDayLight = lightColorScheme(
    primary = Color(0xFFE89A00),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDF9E),
    onPrimaryContainer = Color(0xFF291700),
    secondary = Color(0xFFFF7A6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDAD4),
    onSecondaryContainer = Color(0xFF3D0702),
    tertiary = Color(0xFF6BCB77),
    onTertiary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFBEF),
    onBackground = Color(0xFF221E14),
    surface = Color(0xFFFFFBEF),
    onSurface = Color(0xFF221E14),
    surfaceContainer = Color(0xFFF6EED8),
    surfaceContainerHigh = Color(0xFFEEE5CE),
    surfaceContainerHighest = Color(0xFFE5DCC4)
)

val SunnyDayDark = darkColorScheme(
    primary = Color(0xFFFFBA38),
    onPrimary = Color(0xFF432A00),
    primaryContainer = Color(0xFF623E00),
    onPrimaryContainer = Color(0xFFFFDF9E),
    secondary = Color(0xFFFFB4A9),
    onSecondary = Color(0xFF5D1711),
    tertiary = Color(0xFF88DC92),
    onTertiary = Color(0xFF003914),
    background = Color(0xFF1B170E),
    onBackground = Color(0xFFECE1D3),
    surface = Color(0xFF1B170E),
    onSurface = Color(0xFFECE1D3),
    surfaceContainer = Color(0xFF262116),
    surfaceContainerHigh = Color(0xFF322C1F),
    surfaceContainerHighest = Color(0xFF3F3728)
)
