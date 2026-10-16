# QuickJS
-keep class com.whl.quickjs.** { *; }
-keepclassmembers class com.whl.quickjs.** { *; }
-dontwarn com.whl.quickjs.**

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.lxmusic.mobile.**$$serializer { *; }
-keepclassmembers class com.lxmusic.mobile.** { *** Companion; }
-keepclasseswithmembers class com.lxmusic.mobile.** { kotlinx.serialization.KSerializer serializer(...); }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
