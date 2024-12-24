# Genel kurallar
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses

# Firebase için kurallar
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Gson için kurallar
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Model sınıfları için kurallar
-keep class com.brainfocus.numberdetective.missions.** { *; }
-keep class com.brainfocus.numberdetective.game.** { *; }

# Lottie için kurallar
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# AdMob için kurallar
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Kotlin serialization için kurallar
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Hata ayıklama için kurallar
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
