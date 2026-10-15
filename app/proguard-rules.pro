# ====================================================================
# WeatherFlow — Production ProGuard & R8 Optimization Rules
# Package: com.osquareapps.weather
# ====================================================================

# Keep line numbers and source file names for crash reporting in Firebase Console
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# --------------------------------------------------------------------
# Kotlin & Coroutines
# --------------------------------------------------------------------
-keepclassmembers class * {
    @kotlin.jvm.JvmField *;
}
-dontwarn kotlinx.coroutines.**
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# --------------------------------------------------------------------
# Jetpack Compose & Material 3
# --------------------------------------------------------------------
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# --------------------------------------------------------------------
# Moshi & JSON Serialization
# --------------------------------------------------------------------
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class * extends com.squareup.moshi.JsonAdapter
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}

# Keep all App Data & Network Transfer Models from obfuscation
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }
-keep class com.example.network.** { *; }
-keepclassmembers class com.example.network.** { *; }
-keep class com.example.data.api.** { *; }
-keepclassmembers class com.example.data.api.** { *; }
-keep class com.example.domain.** { *; }
-keepclassmembers class com.example.domain.** { *; }

# --------------------------------------------------------------------
# Retrofit 2 & OkHttp 3 / Okio
# --------------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# --------------------------------------------------------------------
# Room Persistence & SQLite
# --------------------------------------------------------------------
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class com.example.database.** { *; }
-keepclassmembers class com.example.database.** { *; }
-keep class com.example.data.local.** { *; }
-keepclassmembers class com.example.data.local.** { *; }

# --------------------------------------------------------------------
# Google Play Services Location & Hardware
# --------------------------------------------------------------------
-keep class com.google.android.gms.location.** { *; }
-keep interface com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**

# --------------------------------------------------------------------
# Firebase Analytics & App Check
# --------------------------------------------------------------------
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.android.gms.measurement.**

# --------------------------------------------------------------------
# Android WorkManager & Broadcast Receivers
# --------------------------------------------------------------------
-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker
-keep class * extends androidx.work.ListenableWorker

# Android AppWidgets & RemoteViews
-keep class com.example.widget.** { *; }
-keepclassmembers class com.example.widget.** { *; }
-keep class * extends android.appwidget.AppWidgetProvider { *; }

# Android DataStore Preferences
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# --------------------------------------------------------------------
# General Performance Optimizations
# --------------------------------------------------------------------
-repackageclasses ''
-allowaccessmodification
