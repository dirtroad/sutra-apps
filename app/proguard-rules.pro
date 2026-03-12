# Add project specific ProGuard rules here.
-keep class com.sutra.app.** { *; }
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keepattributes *Annotation*
-keepattributes JavascriptInterface
-keepclassmembers class * {
    public <methods>;
}