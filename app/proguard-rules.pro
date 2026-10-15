# Add project specific ProGuard rules here.
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*

# Moshi & JSON Models
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }
-keep class com.example.data.api.** { *; }

# Domain Models
-keep class com.example.domain.** { *; }
-keepclassmembers class com.example.domain.** { *; }

# Room Database & Entities
-keep class androidx.room.** { *; }
-keep class com.example.data.local.** { *; }
-keep class com.example.data.database.** { *; }
-keep class * extends androidx.room.RoomDatabase

# Retrofit & OkHttp
-keepattributes Signature
-keepattributes InnerClasses
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Android Widget Providers
-keep class com.example.widget.** { *; }
