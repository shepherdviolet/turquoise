# Turquoise 1.4.151024
> https://github.com/shepherdviolet/turquoise <br/>

## Description
> Android Library<br/>
> 1.Configurable "Application"/"Activity"... | 可配置的两大组件<br/>
> 2.SlideEngine | 滑动引擎<br/>
> 3.BitmapLoader | 双缓存图片异步加载器<br/>
> 4.some Utils | 工具<br/>

## Modules
> library.turquoise : the "Turquoise" library module  |  库本体 <br/>
> library.demoa : demos of "Turquoise" library  |  示例程序 <br/>

## Export *.aar
>1.build library.turquoise <br/>
>2.get file from : library.turquoise/build/outputs/library.turquoise-release.aar <br/>
>3.rename to "sviolet.turquoise-version.aar" <br/>

## Use *.aar
>1.put "sviolet.turquoise-version.aar" into your module of project (modulename/libs/) <br/>
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
    compile(name:'sviolet.turquoise-version', ext:'aar')
}
```

>3.build your project <br/>

## Releases
> https://github.com/shepherdviolet/turquoise/tree/master/release<br/>
> <br/>
> import source: <br/>
> (1)Project - External Libraries - "sviolet.turquoise-version" Right Button - Library Properties
> (2)add "sviolet.turquoise-source-version.jar"