# Turquoise 1.5.0629

## Description
> Android Library contains View/Utils/Io...

## Modules
> library.turquoise : the "Turquoise" library module  |  库本体
> library.demoa : demos of "Turquoise" library  |  示例程序
> library.librarya : a messy library. IGNORE PLEASE  |  废代码, 请无视

## use .aar
>1.build library
>2.get library.turquoise/build/outputs/library.turquoise-release.aar
>3.put .aar into your module (modulename/libs/)
>edit build.gradle

```java
repositories {
    flatDir {
        dirs 'libs'
    }
}
```

```java
compile(name:'library.turquoise-release', ext:'aar')
```
