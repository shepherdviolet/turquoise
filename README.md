# Turquoise 5.1.170606
* A comprehensive Android library for private use.
* https://github.com/shepherdviolet/turquoise

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)<br/>
<br/>
<br/>
<br/>

# Releases (aar/source)
https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository <br/>
https://git.oschina.net/shepherdviolet/alternate-maven-repository/raw/master/repository <br/>
<br/>
<br/>
<br/>

# Import dependencies from maven repository

```gradle
    repositories {
        maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        compile 'sviolet:turquoise:5.0'
        compile 'sviolet:turquoise-imageloader:5.0'
        compile 'sviolet:turquoise-imageloader-plugin:5.0'
        compile 'sviolet:turquoise-multidex:5.0'
    }
```
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
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-kotlin--stdlib--jre7-dc143c.svg?style=flat)
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

### Kotlin support | sviolet.turquoise.kotlin
* Kotlin extensions: extension function package
* TLogger: Kotlin style
* TFork: Experimental feature, a simple coroutine

### Dependency:
```gradle

    compile project(':turquoise')
    //required
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre7:1.1.2-4"
    compile 'com.android.support:support-v4:25.3.1'
    //optional
    compile 'com.android.support:appcompat-v7:25.3.1'
    compile 'com.android.support:recyclerview-v7:25.3.1'
    compile 'com.squareup.okhttp3:okhttp:3.6.0'

```

<br/>
<br/>
<br/>

# Module turquoise-imageloader
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-kotlin--stdlib--jre7-dc143c.svg?style=flat)

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

### Dependency:
```gradle

    compile project(':turquoise')
    compile project(':turquoise-imageloader')
    //required
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre7:1.1.2-4"
    compile 'com.android.support:support-v4:25.3.1'
    //optional
    compile 'com.squareup.okhttp3:okhttp:3.6.0'

```

### Preview:
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_list.gif)
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_rounded.gif)
<br/>
<br/>
<br/>
<br/>

# Module turquoise-imageloader-plugin
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise--imageloader-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-kotlin--stdlib--jre7-dc143c.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-android--gif--drawable-dc143c.svg?style=flat)](https://github.com/koral--/android-gif-drawable)

###### Expansion Module of `turquoise-imageloader`. Optional.

* Add support for GIF loading, dependent on `pl.droidsonroids.gif:android-gif-drawable:1.1.15`.
* TILoader will load this plugin automatically, as long as your project dependent on this plugin module. Don't need to do anything else.
<br/>

### Dependency:
```gradle

    compile project(':turquoise')
    compile project(':turquoise-imageloader')
    compile project(':turquoise-imageloader-plugin')
    //required
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre7:1.1.2-4"
    compile 'com.android.support:support-v4:25.3.1'
    compile 'pl.droidsonroids.gif:android-gif-drawable:1.1.15'
    //optional
    compile 'com.squareup.okhttp3:okhttp:3.6.0'

```

### Preview:
![](https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/tiloader/tiloader_demo_gif.gif)<br/>
<br/>
<br/>
<br/>

# Module turquoise-multidex
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-kotlin--stdlib--jre7-dc143c.svg?style=flat)

###### Expansion Module of `turquoise`. Optional.

* Add support for MultiDex.
<br/>

### Dependency:
```gradle

    compile project(':turquoise')
    compile project(':turquoise-multidex')
    //required
    compile "org.jetbrains.kotlin:kotlin-stdlib-jre7:1.1.2-4"

```
<br/>
<br/>
<br/>