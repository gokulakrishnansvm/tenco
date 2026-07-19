# Add project specific ProGuard rules here.
# Room, Hilt, Compose, OkHttp, and Firebase ship their own consumer rules.

# ---- kotlinx.serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
# Keep generated serializers and their metadata.
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <methods>;
}
-if @kotlinx.serialization.Serializable class **
-keep, includedescriptorclasses class <1>$$serializer { *; }
-keepclassmembers class ** {
    *** Companion;
}
-keepclasseswithmembers class ** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep all app data/DTO classes (used with reflection-free serialization + Room).
-keep class com.tenco.data.remote.** { *; }
-keep class com.tenco.data.local.** { *; }

# ---- Ktor / OkHttp / Okio ----
-dontwarn org.slf4j.**
-dontwarn kotlinx.coroutines.**
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
