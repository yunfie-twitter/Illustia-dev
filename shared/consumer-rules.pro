# ---------------------------------------------------------------------------
# General Attributes & Line Numbers
# ---------------------------------------------------------------------------
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, SourceFile, LineNumberTable

# ---------------------------------------------------------------------------
# Kotlin Objects, Companions, Singletons & Enums
# ---------------------------------------------------------------------------
# Preserve all classes with INSTANCE and Companion fields (Kotlin objects, data objects, companions, Compose singletons)
-keepclasseswithmembers class * {
    public static final ** INSTANCE;
}

-keepclasseswithmembers class * {
    public static final ** Companion;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class kotlin.jvm.internal.DefaultConstructorMarker { *; }

# ---------------------------------------------------------------------------
# Illustia Shared Library Modules
# ---------------------------------------------------------------------------
-keep class com.yunfie.illustia.** { *; }
-keepclassmembers class com.yunfie.illustia.** { *; }
-keep class com.yunfie.pallasync.** { *; }
-keepclassmembers class com.yunfie.pallasync.** { *; }

# ---------------------------------------------------------------------------
# Jetpack Compose & Compose Multiplatform
# ---------------------------------------------------------------------------
-keepclassmembers class * extends androidx.compose.runtime.snapshots.SnapshotState { *; }
-keepclassmembers class * implements androidx.compose.runtime.State { *; }
-keep class **.ComposableSingletons$* { *; }
-keepclassmembers class **.ComposableSingletons$* {
    public static final ** INSTANCE;
    public static final ** lambda-*;
    public static final ** lambda$*;
    *;
}
-keep class androidx.compose.runtime.internal.ComposableLambdaImpl { *; }
-keep class androidx.compose.runtime.internal.ComposableLambda { *; }

# Compose Multiplatform Resources
-keep class org.jetbrains.compose.resources.** { *; }
-keep class com.yunfie.illustia.Res** { *; }
-keepclassmembers class com.yunfie.illustia.Res** {
    public static final ** INSTANCE;
    *;
}

# ---------------------------------------------------------------------------
# Miuix UI Library
# ---------------------------------------------------------------------------
-keep class top.yukonga.miuix.kmp.** { *; }
-keepclassmembers class top.yukonga.miuix.kmp.** {
    public static final ** INSTANCE;
    *;
}
-dontwarn top.yukonga.miuix.kmp.**

# ---------------------------------------------------------------------------
# Kotlinx Serialization
# ---------------------------------------------------------------------------
-keepclassmembers class * {
    public static final ** Companion;
    public static final ** $serializer;
}
-keepclasseswithmembers class * {
    public static final ** Companion;
}
-keepclasseswithmembers class * {
    public static final ** $serializer;
}
-keepclassmembers @kotlinx.serialization.Serializable class * {
    public static final ** Companion;
    public static final ** $serializer;
    public static final ** INSTANCE;
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    public static final ** INSTANCE;
    *;
}
-dontwarn kotlinx.serialization.**

# ---------------------------------------------------------------------------
# Kotlin Coroutines & Flow
# ---------------------------------------------------------------------------
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ---------------------------------------------------------------------------
# Room Database & Entities
# ---------------------------------------------------------------------------
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Entity class * { *; }
-keep class com.yunfie.illustia.settings.db.** { *; }
-dontwarn androidx.room.paging.**

# ---------------------------------------------------------------------------
# JNA & UniFFI Rust Bindings
# ---------------------------------------------------------------------------
-keep class com.sun.jna.** { *; }
-keep class com.yunfie.illustia.rust.** { *; }
-keep interface com.yunfie.illustia.rust.** { *; }
-keep class * extends com.sun.jna.Structure { *; }
-keep interface * extends com.sun.jna.Library { *; }
-keep interface * extends com.sun.jna.Callback { *; }
-dontwarn com.sun.jna.**
