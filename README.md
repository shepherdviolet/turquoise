# Turquoise 6.3
* A comprehensive Android library for private use.
* https://github.com/shepherdviolet/turquoise

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)<br/>
<br/>
<br/>
<br/>

# Import dependencies from maven repository

```gradle
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }
        maven { url 'https://maven.google.com' }
        //Deprecated way, use jitpack.io instead, just for reference
        //maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        compile 'com.github.shepherdviolet.turquoise:turquoise:6.3'
        compile 'com.github.shepherdviolet.turquoise:turquoise-imageloader:6.3'
    }
```

* Exclude dependencies

https://github.com/shepherdviolet/turquoise/blob/master/doc/ExcludeDependencies.md <br/>

<br/>
<br/>
<br/>

# Import dependencies from aar package

https://github.com/shepherdviolet/turquoise/blob/master/doc/ExportImportManual.md <br/>
<br/>
<br/>
<br/>

# Module turquoise
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-thistle-dc143c.svg?style=flat)](https://github.com/shepherdviolet/thistle)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)

###### The basic library module, as detailed below:

### Utils/Models ┃ sviolet.turquoise.util/utilx/model/modelx
* LifeCycleUtils: monitor LifeCycle of Activity/Fragment
* TLogger: extensible log utils
* EvBus: a simple event bus
* MotionEventUtils: easy to emulate motion event
* cache/bitmap/queue/conversion/crypt/sort/system utils/......

### Views ┃ sviolet.turquoise.ui/uix
* ViewGestureController: easy to build gesture-driven view
* SlideEngine: help to build sliding view, and provides some ready-made Views
* TViewHolder and adapters: easy to build Adapter of View
* VerticalOverDragContainer and indicators: easy to build pull to refresh list view
* LinearIndicatorTabView: easy to build TabView
* shadow/image/gif/ripple/rotate/scrape/dialog......

### Enhanced android component ┃ sviolet.turquoise.enhance
* InjectUtils: inject Views by annotation way
* TApplication/TActivity/TFragment: enhanced Component
* TActivity.executePermissionTask: easy to use Runtime Permission
* WeakHandler: help Handler to avoid Memory Leak(design specifications)

### Kotlin support ┃ sviolet.turquoise.kotlin
* Kotlin extensions: extension function package
* TLogger: Kotlin style
* TFork: Experimental feature, a simple async tools
* TJson: Make json by DSL

<br/>
<br/>
<br/>

# Module turquoise-imageloader
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-android--gif--drawable-dc143c.svg?style=flat)](https://github.com/koral--/android-gif-drawable)

###### TurquoiseImageLoader. it's a new image loader.

### Basic Usage:
```gradle

        //TILoader setting, non-essential
        TILoader.setting(
            new ServerSettings.Builder()
                .setMemoryCachePercent(getApplicationContext(), 0.1f)
                .setDiskCacheSize(10)
                .setLogEnabled(true)
                .build());

        //Node setting, non-essential
        TILoader.node(this).setting(
            new NodeSettings.Builder()
                .setLoadingDrawableFactory(
                    new CommonLoadingDrawableFactory()
                        .setImageResId(R.mipmap.loading_image)
                        .setBackgroundColor(0xFFF0F0F0))
                .build());

        //Load setting, non-essential
        Params params = new Params.Builder()
            .setBitmapConfig(Bitmap.Config.ARGB_8888)
            .build();

        //load image
        TILoader.node(this).load(url, params, imageView);

```

### Preview:
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_list.gif)
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_rounded.gif)
![](https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/tiloader/tiloader_demo_gif.gif)<br/>
<br/>
<br/>
<br/>
