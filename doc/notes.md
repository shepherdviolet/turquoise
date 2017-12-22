# android gradle plugin 3.1.0-alpha06的问题
* 开启InstantRun的情况下, 编译程序会检查所有类, 使用低版本手机调试时, 一些高版本API会报出类不存在导致编译失败, 关闭InstantRun可以避开此问题
* 关闭InstantRun的情况下, 编译出来的APK有问题, 开启MultiDex时会有Activity出现ClassNotFound, 关闭MultiDex后也会有自定义View出现ClassNotFound