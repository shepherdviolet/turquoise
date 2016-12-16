# Turquoise 3.4.161213
* A comprehensive Android library for private use.
* https://github.com/shepherdviolet/turquoise

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)<br/>
<br/>
<br/>
<br/>

# Module turquoise
![API](https://img.shields.io/badge/API-10%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)

###### The basic library module, as detailed below:

### Utils/Models ┃ sviolet.turquoise.util/utilx/model/modelx
* LifeCycleUtils: monitor LifeCycle of Activity/Fragment
* TLogger: extensible log utils
* MotionEventUtils: easy to emulate motion event
* cache/bitmap/queue/conversion/crypt/sort/system utils/......

### Views ┃ sviolet.turquoise.ui/uix
* ViewGestureController: easy to build gesture-driven view
* SlideEngine: help to build sliding view, and provides some ready-made Views
* TViewHolder and adapters: easy to build Adapter of View
* VerticalOverDragContainer and indicators: easy to build pull to refresh list view
* LinearIndicatorTabView: easy to build TabView
* shadow/image/gif/ripple/rotate/scrape......

### enhanced android component ┃ sviolet.turquoise.enhance
* InjectUtils: inject Views by annotation way
* TApplication/TActivity/TFragment: enhanced Component
* TActivity.executePermissionTask: easy to use Runtime Permission
* WeakHandler: help Handler to avoid Memory Leak(design specifications)

### Dependency:
```java

    compile project(':turquoise')
    //required
    compile 'com.android.support:support-v4:24.2.1'
    //optional
    compile 'com.android.support:appcompat-v7:24.2.1'
    compile 'com.android.support:recyclerview-v7:24.2.1'
    compile 'org.bouncycastle:bcprov-jdk16:1.46'
    compile 'com.squareup.okhttp3:okhttp:3.3.1'

```

<br/>
<br/>
<br/>

# Module turquoise.imageloader
![API](https://img.shields.io/badge/API-11%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)

###### TurquoiseImageLoader. it's a new image loader.

### Basic Usage:
```java

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

### Dependency:
```java

    compile project(':turquoise')
    compile project(':turquoise.imageloader')
    //required
    compile 'com.android.support:support-v4:24.2.1'
    //optional
    compile 'com.squareup.okhttp3:okhttp:3.3.1'

```

### Preview:
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_list.gif)
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_rounded.gif)
<br/>
<br/>
<br/>
<br/>

# Module turquoise.imageloader.plugin
![API](https://img.shields.io/badge/API-11%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise.imageloader-2ed8a8.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-android--gif--drawable-dc143c.svg?style=flat)](https://github.com/koral--/android-gif-drawable)

###### Expansion Module of `turquoise.imageloader`. Optional.

* Add support for GIF loading, dependent on `pl.droidsonroids.gif:android-gif-drawable:1.1.15`.
* TILoader will load this plugin automatically, as long as your project dependent on this plugin module. Don't need to do anything else.
<br/>

### Dependency:
```java

    compile project(':turquoise')
    compile project(':turquoise.imageloader')
    compile project(':turquoise.imageloader.plugin')
    //required
    compile 'com.android.support:support-v4:24.2.1'
    compile 'pl.droidsonroids.gif:android-gif-drawable:1.1.15'
    //optional
    compile 'com.squareup.okhttp3:okhttp:3.3.1'

```

### Preview:
![](https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/tiloader/tiloader_demo_gif.gif)<br/>
<br/>
<br/>
<br/>

# Package Releases (aar/source)
https://github.com/shepherdviolet/static-resources/tree/master/turquoise-release <br/>
<br/>
<br/>
<br/>

# How to import Turquoise into your project
https://github.com/shepherdviolet/turquoise/blob/master/doc/ExportImportManual.md <br/>
<br/>
<br/>
<br/>