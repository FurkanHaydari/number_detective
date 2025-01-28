# General rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses

# AdMob rules
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Firebase rules
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Koin rules
-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }
-keepclassmembers class * { @org.koin.core.annotation.KoinInject *; }

# Model classes rules
-keep class com.brainfocus.numberdetective.model.** { *; }
-keep class com.brainfocus.numberdetective.game.** { *; }
-keep class com.brainfocus.numberdetective.viewmodel.** { *; }

# MPAndroidChart rules
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Lottie rules
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# Debug rules
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# Kotlin coroutines rules
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Lifecycle components
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}
