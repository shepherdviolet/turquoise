# Turquoise 1.5.151102
> https://github.com/shepherdviolet/turquoise <br/>

## Description
> Android Library<br/>
> 1.Configurable "Application"/"Activity"... | 可配置的两大组件<br/>
> 2.SlideEngine | 滑动引擎<br/>
> 3.BitmapLoader | 双缓存图片异步加载器<br/>
> 4.some Utils | 工具<br/>

## Modules
> turquoise : the "Turquoise" library module  |  库本体 <br/>
> demoa : demos of "Turquoise" library  |  示例程序 <br/>

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

## Releases
> https://github.com/shepherdviolet/turquoise/tree/master/release <br/>
> <br/>
> import source: <br/>
> (1)Project - External Libraries - "turquoise-version" Right Button - Library Properties <br/>
> (2)add "turquoise-source-version.jar" <br/>