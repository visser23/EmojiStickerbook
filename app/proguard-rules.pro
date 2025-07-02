# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep your application class
-keep class io.github.storybookemoji.StoryBookEmojiApplication { *; }

# Keep model classes
-keep class io.github.storybookemoji.model.** { *; }

# Compose rules
-keepclasseswithmembers class * {
    @androidx.compose.ui.tooling.preview.Preview <methods>;
}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# ========================================
# EMOJI STICKER BOOK PERFORMANCE RULES
# ========================================

# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-keep class kotlin.coroutines.** { *; }

# Optimize Kotlin metadata
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeVisibleTypeAnnotations

# Remove debug logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Remove println statements in release builds
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}

# Remove System.out.println in release builds
-assumenosideeffects class java.lang.System {
    public static void out.println(...);
    public static void out.print(...);
}

# Optimize enums
-optimizations !code/simplification/enum

# Keep ViewModel classes
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Keep Use Case classes
-keep class io.github.storybookemoji.domain.usecases.** { *; }

# Aggressive optimization settings
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove unused code
-dontwarn **
-ignorewarnings

# Keep essential Android classes
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Security: Obfuscate all non-essential classes
-repackageclasses 'obfuscated'

# Performance: Enable all optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Emoji2
-keep class androidx.emoji2.** { *; }

# Material3
-keep class androidx.compose.material3.** { *; }