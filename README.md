# Turquoise 3.2.160525
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
* ~~3.BitmapLoader/SimpleBitmapLoader: deprecated, use TILoader instead (for API11 above)~~
* cache/queue/conversion/crypt/sort/android system utils/......

### Views ┃ sviolet.turquoise.ui/uix
* SlideEngine: help to build sliding view, and provides some ready-made Views
* TViewHolder: easy to build Adapter of View
* shadow/gif/ripple/rotate/scrape/......

### enhanced android component ┃ sviolet.turquoise.enhance
* InjectUtils: inject Views by annotation way
* TApplication/TActivity: enhanced Component
* TActivity.executePermissionTask: easy to use Runtime Permission
* WeakHandler: help Handler to avoid Memory Leak(design specifications)

<br/>
<br/>
<br/>

# Module turquoise.v7
![API](https://img.shields.io/badge/API-10%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v7-dc143c.svg?style=flat)

###### Expansion Module of `turquoise`, Add support for `support-v7`. Optional.

* Add support for AppCompatActivity.
* Add support for RecyclerView.

<br/>
<br/>
<br/>

# Module turquoise.imageloader
![API](https://img.shields.io/badge/API-11%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)

###### TurquoiseImageLoader. it's a new ImageLoader instead of BitmapLoader/SimpleBitmapLoader.
<br/>
### Overview:
![](https://github.com/shepherdviolet/turquoise/blob/master/doc/turquoise-overview.png)
<br/>
### Basic Usage:
```java

        //TILoader setting, Non essential
        TILoader.setting(
            new ServerSettings.Builder()
                .setMemoryCachePercent(getApplicationContext(), 0.1f)
                .setDiskCacheSize(10)
                .setLogEnabled(true)
                .build());

        //Node setting, Non essential
        TILoader.node(this).setting(
            new NodeSettings.Builder()
                .setNetworkLoadHandler(new MyNetworkLoadHandler())
                .setLoadingDrawableFactory(
                    new CommonLoadingDrawableFactory()
                        .setImageResId(R.mipmap.loading_image)
                        .setBackgroundColor(0xFFF0F0F0))
                .build());

        //Load setting, Non essential
        Params params = new Params.Builder()
            .setBitmapConfig(Bitmap.Config.ARGB_8888)
            .build();

        //load image
        TILoader.node(this).load(url, params, imageView);

```
<br/>
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
<br/>
### Usage:
* `TILoader` will load this plugin automatically, as long as your project dependent on this module.
* `Don't need to do anything else`.

```java

    compile project(':turquoise.imageloader')
    compile project(':turquoise.imageloader.plugin')

```

<br/>
### Preview:
![](https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/tiloader/tiloader_demo_gif.gif)
<br/>
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