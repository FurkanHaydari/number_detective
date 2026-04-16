# General rules - Essential for Kotlin & R8
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Exceptions,InnerClasses

# AdMob rules
# Note: Modern AdMob SDK includes its own rules, but these can be safety nets if you use custom mediation.
# Removing the broad 'gms.**' rule to resolve warnings.

# Hilt / Dagger rules
-keep class dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# DataStore & Protobuf (if used)
-dontwarn androidx.datastore.**

# Number Detective - Modern Feature-based Architecture
-keep class com.brainfocus.numberdetective.data.model.** { *; }

# Obfuscation settings
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
