# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Applications/Android Studio.app/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Obfuscation parameters:
#-dontobfuscate
-useuniqueclassmembernames
-keepattributes SourceFile,LineNumberTable
-allowaccessmodification

-dontwarn com.google.android.gms.**
-dontwarn com.squareup.**
-dontwarn com.google.appengine.api.urlfetch.**
-dontwarn it.**
# http://stackoverflow.com/questions/18646899/proguard-cant-find-referenced-class-com-google-android-gms-r
-keep public class com.google.android.gms.* { public *; }

#-Firebase Auth recommandations
-keepattributes Signature
-keepattributes *Annotation*

# ----------------------
# Butterknife
-dontwarn com.jakewharton.**
-dontwarn butterknife.internal.**
-keep class butterknife.** { *; }
-keep class **$$ViewInjector { *; }
-keep class **$$ViewBinder { *; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }
# ----------------------



# ----------------------
# GSON and Jackson
# Keep the pojos used by GSON or Jackson
-keep class com.futurice.project.models.pojo.** { *; }

# Keep GSON stuff
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }

# Keep Jackson stuff
-keep class org.codehaus.** { *; }
-keep class com.fasterxml.jackson.annotation.** { *; }

# Keep these for GSON and Jackson
-keepattributes *Annotation*
-keepattributes EnclosingMethod
# ----------------------



# ----------------------
# Keep Retrofit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keep class com.google.inject.** { *; }
-keep class org.apache.http.** { *; }
-keep class javax.inject.** { *; }
-keepattributes Signature
-keepattributes Exceptions
-keepclassmembernames interface * {
    @retrofit.http.* <methods>;
}
# ----------------------



# ----------------------
# Keep Picasso
-keep class com.squareup.picasso.** { *; }
-keepclasseswithmembers class * {
    @com.squareup.picasso.** *;
}
-keepclassmembers class * {
    @com.squareup.picasso.** *;
}
# ----------------------
