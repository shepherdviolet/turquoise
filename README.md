# Turquoise 1.5.0709

## Description
> Android Library contains View/Utils/Io...

## Modules
> library.turquoise : the "Turquoise" library module  |  库本体 <br/>
> library.demoa : demos of "Turquoise" library  |  示例程序 <br/>
> library.librarya : a messy library. IGNORE PLEASE  |  废代码, 请无视 <br/>

## Use *.aar
>1.build library <br/>
>2.get library.turquoise/build/outputs/library.turquoise-release.aar <br/>
>3.put .aar into your module (modulename/libs/) <br/>
>4.edit build.gradle <br/>

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
    compile(name:'library.turquoise-release', ext:'aar')
}
```
