# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#
#
#-keeppackagenames org.jsoup.nodes
#-ignorewarnings
#-keep class * {
#    public private *;
#}

-keepnames class org.jsoup.nodes.Entities
-keepattributes *Annotation*, Signature, Exception
#-repackageclasses
-repackageclasses ''

#-keepclassmembers class * {
#    public void *test*(...);
#}

#-keep public class org.jsoup.** {
#    public *;
#}

-keep class com.diyandroid.eazycampus.activity.HomePage.Story{ *; }
-keep class android.support.v7.widget.SearchView { *; }
-keep class com.zl.reik.dilatingdotsprogressbar.DilatingDotDrawable { *; }