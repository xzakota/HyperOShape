# Add project specific ProGuard rules here.
# Keep Xposed related classes
-keep class de.robv.android.xposed.** { *; }
-keep class io.github.libxposed.api.** { *; }
-keep class * implements de.robv.android.xposed.IXposedHookZygoteInit
-keep class * implements de.robv.android.xposed.IXposedHookLoadPackage
-keep class * implements io.github.libxposed.api.XposedModule
