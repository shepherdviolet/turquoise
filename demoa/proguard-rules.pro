# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
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

#压缩级别
-optimizationpasses 5
#包名不混合大小写
-dontusemixedcaseclassnames
#不忽略非公共的库类
-dontskipnonpubliclibraryclasses
#优化 不优化输入的类文件
-dontoptimize
#预校验
-dontpreverify
#混淆时是否记录日志
-verbose
#混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#保护注解
-keepattributes *Annotation*
#保持哪些类不被混淆
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
#如果有引用v4包可以添加下面这行
#-keep public class * extends android.support.v4.app.Fragment
#忽略警告
-ignorewarning
#####################记录生成的日志数据,在本项目根目录输出################
#列出apk包内所有class的内部结构
-dump buildlog/classes.txt
#未混淆的类和成员
-printseeds buildlog/seeds.txt
#列出从apk中删除的代码
-printusage buildlog/unused.txt
#混淆前后的映射
-printmapping buildlog/mapping.txt
#####################避免混淆参数####################
#避免混淆注释
-keepattributes *Annotation*
#避免混淆泛型 如果混淆报错建议关掉
#-keepattributes Signature
#####################引用第三方jar包#########################
#混淆时引用的jar包
#-libraryjars libs/name.jar
#需要混淆的jar包(不常用)
#-injars libs/name.jar
#需要输出的jar包(不常用)
#-outjars libs/name.jar
#####################避免混淆包/类#########################
#忽略警告
-dontwarn sviolet.turquoise.**
#保留一个完整的包
-keep class sviolet.turquoise.** {
    *;
 }
#如果引用了v4或者v7包
#-dontwarn android.support.**
############避免native方法混淆#################
#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
############避免自定义控件混淆##################
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}
#-keepclasseswithmembers class * {
#    public <init>(android.content.Context, android.util.AttributeSet, int);
#}
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
############避免常用类混淆##################
#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable
#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#保持枚举 enum 类不被混淆 如果混淆报错，建议直接使用上面的 -keepclassmembers class * implements java.io.Serializable即可
#-keepclassmembers enum * {
#  public static **[] values();
#  public static ** valueOf(java.lang.String);
#}
-keepclassmembers class * {
    public void *ButtonClicked(android.view.View);
}
#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}

#移除log 测试了下没有用还是建议自己定义一个开关控制是否输出日志
#-assumenosideeffects class android.util.Log {
#    public static boolean isLoggable(java.lang.String, int);
#    public static int v(...);
#    public static int i(...);
#    public static int w(...);
#    public static int d(...);
#    public static int e(...);
#}

###################避免WebView回调的JS接口混淆######################

#-keep class sviolet.WebViewActivity
#-keep class sviolet.WebViewActivity$*{
#    <methods>;
#}
#-keepclassmembers class sviolet.WebViewActivity$*{
#    *;
#}