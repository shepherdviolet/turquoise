# Turquoise 3.0.160503
> A comprehensive Android library for private use.<br/>
> https://github.com/shepherdviolet/turquoise <br/>

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)<br/>

## Module "turquoise"
> API level 10 above.<br/>
> Dependency: support-v4;<br/>
> <br/>
> The basic library module, as detailed below:<br/>
> <br/>
> Utils/Models ┃ sviolet.turquoise.util/utilx/model/modelx <br/>
> ┈┈ 1.LifeCycleUtils: monitor LifeCycle of Activity/Fragment<br/>
> ┈┈ 2.TLogger: extensible log utils<br/>
> ┈┈ ~~3.BitmapLoader/SimpleBitmapLoader: deprecated, use TILoader instead (for API11 above)~~<br/>
> ┈┈ cache/queue/conversion/crypt/sort/android system utils/......<br/>
> <br/>
> Views ┃ sviolet.turquoise.ui/uix <br/>
> ┈┈ 1.SlideEngine: help to build sliding view, and provides some ready-made Views<br/>
> ┈┈ 2.TViewHolder: easy to build Adapter of View<br/>
> ┈┈ shadow/gif/ripple/rotate/scrape/......<br/>
> <br/>
> enhanced android component ┃ sviolet.turquoise.enhance <br/>
> ┈┈ 1.InjectUtils: inject Views by annotation way<br/>
> ┈┈ 2.TApplication/TActivity: enhanced Component<br/>
> ┈┈ 3.TActivity.executePermissionTask: easy to use Runtime Permission<br/>
> ┈┈ 4.WeakHandler: help Handler to avoid Memory Leak(design specifications)<br/>

## Module "turquoise.v7"
> API level 10 above.<br/>
> Dependency: turquoise, support-v7;<br/>
> <br/>
> Expansion Module of "turquoise", Add support for "support-v7".<br/>
> <br/>
> 1.Add support for AppCompatActivity.<br/>
> 2.Add support for RecyclerView.<br/>

## Module "turquoise.imageloader"
> API level 11 above.<br/>
> Dependency: turquoise;<br/>
> <br/>
> TurquoiseImageLoader. it's a new ImageLoader instead of BitmapLoader/SimpleBitmapLoader.<br/>
> Alpha version release!!!<br/>
> <br/>
> TO DO : <br/>
> 1.architecture diagram<br/>
> 2.annotate<br/>
> 3.demo<br/>
> 4.user guide<br/>
> <br/>
> Overview:<br/>
![](https://github.com/shepherdviolet/turquoise/blob/master/doc/turquoise-overview.png)<br/>
> <br/>
> Basic Usage:<br/>

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

## Package Releases (aar/source)
> https://github.com/shepherdviolet/static-resources/tree/master/turquoise-release <br/>

## How to import Turquoise into your project
> https://github.com/shepherdviolet/turquoise/blob/master/doc/ExportImportManual.md <br/>