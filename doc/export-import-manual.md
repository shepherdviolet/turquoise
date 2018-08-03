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

```gradle
repositories {
    ......
    flatDir {
        dirs 'libs'
    }
}
```

```gradle
dependencies {
    ......
    compile(name:'turquoise-version', ext:'aar')
}
```

>3.build your project <br/>

## Import source(jar)
> 1.Project - External Libraries - "turquoise-version" Right Button - Library Properties <br/>
> 2.add "turquoise-source-version.jar" <br/>