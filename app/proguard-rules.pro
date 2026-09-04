# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep essential attributes for debugging and debugging stack traces
-keepattributes SourceFile,LineNumberTable,LocalVariableTable,LocalVariableTypeTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# Room database rules
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Moshi rules (Moshi is used for JSON serialization in the app)
-dontwarn com.squareup.moshi.**
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonQualifier class *
-keepclassmembers class * {
    @com.squareup.moshi.Json class *;
}
-keep class *JsonAdapter { public <init>(...); }

# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
# A resource is loaded with a relative path so the name of this class must be preserved
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin Coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler
-keepclassmembernames class kotlinx.coroutines.android.HandlerContext$* { *; }
-dontwarn kotlinx.coroutines.**

# Keep our UI state models and database entities from being obfuscated or stripped
-keep class com.example.ui.** { *; }
-keep class com.example.data.** { *; }
