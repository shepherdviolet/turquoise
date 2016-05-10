# Turquoise 3.0.160510
> A comprehensive Android library for private use.<br/>
> https://github.com/shepherdviolet/turquoise <br/>

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)<br/>

## Module `turquoise`
![API](https://img.shields.io/badge/API-10%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)
> The basic library module, as detailed below:<br/>
### Utils/Models ┃ sviolet.turquoise.util/utilx/model/modelx <br/>
> ┈┈ 1.LifeCycleUtils: monitor LifeCycle of Activity/Fragment<br/>
> ┈┈ 2.TLogger: extensible log utils<br/>
> ┈┈ ~~3.BitmapLoader/SimpleBitmapLoader: deprecated, use TILoader instead (for API11 above)~~<br/>
> ┈┈ cache/queue/conversion/crypt/sort/android system utils/......<br/>
### Views ┃ sviolet.turquoise.ui/uix <br/>
> ┈┈ 1.SlideEngine: help to build sliding view, and provides some ready-made Views<br/>
> ┈┈ 2.TViewHolder: easy to build Adapter of View<br/>
> ┈┈ shadow/gif/ripple/rotate/scrape/......<br/>
### enhanced android component ┃ sviolet.turquoise.enhance <br/>
> ┈┈ 1.InjectUtils: inject Views by annotation way<br/>
> ┈┈ 2.TApplication/TActivity: enhanced Component<br/>
> ┈┈ 3.TActivity.executePermissionTask: easy to use Runtime Permission<br/>
> ┈┈ 4.WeakHandler: help Handler to avoid Memory Leak(design specifications)<br/>

## Module `turquoise.v7`
![API](https://img.shields.io/badge/API-10%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v7-dc143c.svg?style=flat)
> Expansion Module of `turquoise`, Add support for `support-v7`. Optional.<br/>
> 1.Add support for AppCompatActivity.<br/>
> 2.Add support for RecyclerView.<br/>

## Module `turquoise.imageloader`
![API](https://img.shields.io/badge/API-11%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
> TurquoiseImageLoader. it's a new ImageLoader instead of BitmapLoader/SimpleBitmapLoader.<br/>
### Overview:<br/>
![](https://github.com/shepherdviolet/turquoise/blob/master/doc/turquoise-overview.png)<br/>
### Basic Usage:<br/>

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

## Module `turquoise.imageloader.plugin`
![API](https://img.shields.io/badge/API-11%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise.imageloader-2ed8a8.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-android--gif--drawable-dc143c.svg?style=flat)](https://github.com/koral--/android-gif-drawable)
> Expansion Module of `turquoise.imageloader`. Optional.<br/>
> 1.Add support for GIF loading, dependent on `pl.droidsonroids.gif:android-gif-drawable:1.1.15`.<br/>
### Usage:<br/>
> `TILoader` will load this plugin automatically, as long as your project dependent on this module.<br/>
> `Don't need to do anything else`.<br/>

```java

    compile project(':turquoise.imageloader')
    compile project(':turquoise.imageloader.plugin')

```

## Package Releases (aar/source)
> https://github.com/shepherdviolet/static-resources/tree/master/turquoise-release <br/>

## How to import Turquoise into your project
> https://github.com/shepherdviolet/turquoise/blob/master/doc/ExportImportManual.md <br/>