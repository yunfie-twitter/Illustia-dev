# ---------------------------------------------------------------------------
# Kotlin Objects, Companions & Enums
# ---------------------------------------------------------------------------
# Preserve Kotlin object INSTANCE and Companion fields so runtime accesses
# (direct, Composable singletons, or reflected) do not fail with NoSuchFieldError.
-keepclassmembers class * {
    public static final ** INSTANCE;
    public static final ** Companion;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class kotlin.jvm.internal.DefaultConstructorMarker { *; }

# ---------------------------------------------------------------------------
# Jetpack Compose & Compose Multiplatform
# ---------------------------------------------------------------------------
# Keep Compose snapshot states, remember keys, and composition locals
-keepclassmembers class * extends androidx.compose.runtime.snapshots.SnapshotState { *; }
-keepclassmembers class * implements androidx.compose.runtime.State { *; }

# Compose Compiler generated singletons and lambdas
-keep class **.ComposableSingletons$* { *; }
-keepclassmembers class **.ComposableSingletons$* {
    public static final ** INSTANCE;
    public static final ** lambda-*;
    public static final ** lambda$*;
}
-keep class androidx.compose.runtime.internal.ComposableLambdaImpl { *; }

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
-keepattributes *Annotation*, InnerClasses, EnclosingMethod
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
# Coil Image Loading
# ---------------------------------------------------------------------------
-keep class coil3.** { *; }
-dontwarn coil3.**

# ---------------------------------------------------------------------------
# OkHttp & Okio
# ---------------------------------------------------------------------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class okhttp3.internal.publicsuffix.PublicSuffixDatabase {
    private java.lang.String[] publicSuffixList;
    private java.lang.String[] publicSuffixExceptionList;
}
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# ---------------------------------------------------------------------------
# Tink / Security-Crypto
# ---------------------------------------------------------------------------
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.j2objc.annotations.**

# ---------------------------------------------------------------------------
# JNA & UniFFI Rust Bindings
# ---------------------------------------------------------------------------
# JNA is used by the generated UniFFI Rust bindings. Its native dispatcher
# resolves Java classes and fields by their original JNI names at runtime.
-keep class com.sun.jna.** { *; }
-keep class com.yunfie.illustia.rust.** { *; }
-keep interface com.yunfie.illustia.rust.** { *; }
-keep class * extends com.sun.jna.Structure { *; }
-keep interface * extends com.sun.jna.Library { *; }
-keep interface * extends com.sun.jna.Callback { *; }
-dontwarn com.sun.jna.**

# ---------------------------------------------------------------------------
# PallaSync & Settings
# ---------------------------------------------------------------------------
-keep class com.yunfie.pallasync.** { *; }
-keep class com.yunfie.illustia.sync.** { *; }
-keep class com.yunfie.illustia.settings.** { *; }
-keep class com.yunfie.illustia.models.** { *; }
