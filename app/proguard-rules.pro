# General rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# AdMob rules
-keep public class com.google.android.gms.ads.** { public *; }
-keep public class com.google.ads.** { public *; }

# Model classes rules
-keep class com.brainfocus.numberdetective.game.** { *; }

# Debug rules
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable
