# SongSync release rules
#
# Keep application classes intact: Hilt components, Media3 session callbacks,
# WorkManager workers, Room entities and Glance widgets are all referenced
# reflectively or generated at build time. R8 still shrinks and optimizes the
# third-party dependencies.

-keep class com.songsync.** { *; }

# Hilt / Dagger generated components
-keep,allowobfuscation @interface dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper

# Media3 / ExoPlayer
-dontwarn androidx.media3.**

# Room
-keep class * extends androidx.room.RoomDatabase

# WorkManager
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker

# Kotlin serialization (safe navigation args and JSON backup/restore)
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.songsync.**$$serializer { *; }
-keepclassmembers class com.songsync.** {
    *** Companion;
}
-keepclasseswithmembers class com.songsync.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Coil
-dontwarn coil.**
