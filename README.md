# Turquoise 2.0.160304
> a comprehensive Android Library for private use <br/>
> https://github.com/shepherdviolet/turquoise <br/>

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)

## Module "turquoise"
> the basic library module, as detailed below:<br/>
> <br/>
> Utils/Models ┃ sviolet.turquoise.util/utilx/model/modelx <br/>
> ┈┈ 1.BitmapLoader/SimpleBitmapLoader: a simple bitmap loader with memory/disk caches<br/>
> ┈┈ 2.LifeCycleUtils: monitor LifeCycle of Activity/Fragment<br/>
> ┈┈ 3.TLogger: extensible log utils<br/>
> ┈┈ cache/queue/conversion/crypt/sort/android system utils/......<br/>
> <br/>
> Views ┃ sviolet.turquoise.ui/uix <br/>
> ┈┈ 1.SlideEngine: help to build sliding view, and provides some ready-made Views<br/>
> ┈┈ 2.ViewHolder: easy to build Adapter of View<br/>
> ┈┈ shadow/gif/ripple/rotate/scrape/......<br/>
> <br/>
> enhanced android component ┃ sviolet.turquoise.enhance <br/>
> ┈┈ 1.InjectUtils: inject Views by annotation way<br/>
> ┈┈ 2.TApplication/TActivity: enhanced Component<br/>
> ┈┈ 3.TActivity.executePermissionTask: easy to use Runtime Permission<br/>
> ┈┈ 4.WeakHandler: help Handler to avoid Memory Leak(design specifications)<br/>
> <br/>

## Module "turquoise.imageloader"
> TurquoiseImageLoader, dependent on module "turquoise" <br/>
> <br/>
> NOT AVAILABLE!!! still developing...<br/>
> it's a new ImageLoader instead of BitmapLoader/SimpleBitmapLoader.<br/>
> <br/>

## Module "demoa"
> demos of Turquoise library<br/>
> <br/>

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