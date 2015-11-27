# Turquoise 1.4.151127
> https://github.com/shepherdviolet/turquoise <br/>

## Description
> Android Library <br/>
> <br/>
> Utils ┃ sviolet.turquoise.utils <br/>
> ┈┈ 1.BitmapLoader/SimpleBitmapLoader ┃ 双缓存图片异步加载器 ┃ sviolet.turquoise.utils.bitmap.loader <br/>
> ┈┈ 2.LifeCycleUtils ┃ Activity生命周期监听工具 ┃ sviolet.turquoise.utils.lifecycle <br/>
> <br/>
> Views ┃ sviolet.turquoise.view <br/>
> ┈┈ 1.SlideEngine ┃ 滑动引擎 ┃ sviolet.turquoise.view.slide <br/>
> <br/>
> enhanced Component ┃ sviolet.turquoise.enhanced <br/>
> ┈┈ 1.InjectUtils ┃ 注释型控件注入工具 ┃ sviolet.turquoise.enhanced.utils <br/>
> ┈┈ 2.TApplication/TActivity ┃ 可配置组件 ┃ sviolet.turquoise.enhanced <br/>
> ┈┈ 3.TActivity.executePermissionTask ┃ 回调方式请求权限 ┃ sviolet.turquoise.enhanced <br/>

## Modules
> 1.turquoise ┃ the "Turquoise" library module ┃ 库本体 <br/>
> 2.demoa ┃ demos of "Turquoise" library ┃ 示例程序 <br/>

## Package Releases (aar/source/javadoc)
> https://github.com/shepherdviolet/static-resources/tree/master/turquoise-release <br/>

## Export *.aar
>1.build module "turquoise" <br/>
>2.get file from : turquoise/build/outputs/turquoise-release.aar <br/>
>3.rename to "turquoise-version.aar" <br/>

## Import *.aar as module
>1.File - New - New Module - import .JAR/.AAR package - Next <br/>
>2.select the file "turquoise-version.aar" - Finish <br/>
>3.F4 - select your module - dependencies - Module Dependency <br/>
>4.select the module "turquoise" <br/>

## Import *.aar as libs
>1.put "turquoise-version.aar" into your module of project (module-name/libs/) <br/>
>2.edit module's build.gradle <br/>

```java
repositories {
    ......
    flatDir {
        dirs 'libs'
    }
}
```

```java
dependencies {
    ......
    compile(name:'turquoise-version', ext:'aar')
}
```

>3.build your project <br/>

## Import source(jar)
> 1.Project - External Libraries - "turquoise-version" Right Button - Library Properties <br/>
> 2.add "turquoise-source-version.jar" <br/>