# SongSync 🎵

SongSync is a modern, fully offline native Android music player designed with Material 3 Expressive and Jetpack Compose.

## Key Highlights

- **100% Offline & Private**: Zero network usage. Does **not** request or declare `android.permission.INTERNET`.
- **Zero Mock Data**: Audio library is populated directly from the device's `MediaStore` with incremental scanning and WorkManager background sync.
- **Material 3 Expressive Design**: Expressive spring motion schemes, custom shapes (Cookie9Sided, SoftBurst), and 5 curated color palettes (Peach Pop, Mint Soda, Lavender Dream, Bubblegum, Sunny Day, plus AMOLED black mode).
- **Media3 Architecture**: Background playback via `MediaLibraryService` (`SongSyncMediaService`), complete with lock screen controls, rich media notifications, Android Auto browse tree, and Glance home screen widgets.
- **Audio Engine**: Equalizer with presets, Bass Boost, Virtualizer, Loudness Enhancer, ReplayGain normalization, gapless playback, crossfade (0–12s), speed (0.5x–2.0x), and pitch control.
- **Synchronized Lyrics**: Embedded tag parsing with sidecar `.lrc` file support, smooth auto-scroll, sync offset adjustment, and plain-text fallback.
- **Tag Editor**: In-app metadata editor for title, artist, album, genre, year, and track number with Android 11+ write-permission flow.

## Architecture

SongSync follows Clean Architecture and Unidirectional Data Flow (UDF) across multi-module boundaries:

```
├── app                  # Entry point, MainActivity, Navigation Graph, Glance AppWidget
├── core
│   ├── common           # Result wrapper, Coroutine dispatchers, common utilities
│   ├── domain           # Domain models, Repository contracts, Business entities
│   ├── data             # Room database, Entities, DAOs, DataStore, Repository implementations
│   └── ui               # Material 3 Expressive theme, Palettes, Custom Shapes, Mascot, UI components
├── media                # Media3 ExoPlayer, MediaLibraryService, Equalizer, SleepTimer, MediaStore scanner
└── feature
    ├── library          # Tabs (Songs, Albums, Artists, Playlists, Genres, Folders), Search, Tag Editor, Equalizer UI
    ├── player           # Full-screen Now Playing, Mini Player, Queue, Lyrics, Speed/Pitch, Sleep Timer
    └── settings         # Theme selector, Palette picker, Playback settings, JSON Backup/Restore, Onboarding
```

## Tech Stack

- **UI**: Jetpack Compose & Material 3 Expressive (`androidx.compose.material3:material3:1.4.0-alpha08`)
- **Audio**: AndroidX Media3 (`androidx.media3:media3-exoplayer:1.5.1`, `media3-session:1.5.1`)
- **Dependency Injection**: Dagger Hilt (`2.55`)
- **Database & Storage**: Room (`2.7.0-alpha13`) with KSP, Jetpack DataStore Preferences
- **Background Tasks**: WorkManager (`2.10.0`) with `HiltWorkerFactory`
- **Widget**: AndroidX Glance (`1.1.1`)
- **Image Loading**: Coil 3 (`3.1.0`)

## Building and Running

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- JDK 17 or JDK 21
- Android SDK 35 (compileSdk 35, minSdk 26)

### Gradle Tasks
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests across all modules
./gradlew test

# Run lint checks
./gradlew lintDebug
```
