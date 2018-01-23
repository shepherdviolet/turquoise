# Turquoise 8.0
###### A comprehensive Android library for private use.
###### https://github.com/shepherdviolet/turquoise

![](https://github.com/shepherdviolet/static-resources/blob/master/image/logo/turquoise.jpg)<br/>

* Import dependencies from maven repository

```gradle
    repositories {
        //Local repository
        //maven { url 'file:C:/m2repository/repository' }
        jcenter()
        maven { url 'https://jitpack.io' }
        google()
        //Deprecated way, use jitpack.io instead, just for reference
        //maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        compile 'com.github.shepherdviolet.turquoise:turquoise:8.0'
        compile 'com.github.shepherdviolet.turquoise:turquoise-imageloader:8.0'
    }
```

* How to exclude dependencies

https://github.com/shepherdviolet/turquoise/blob/master/doc/ExcludeDependencies.md <br/>

* Import dependencies from aar package

https://github.com/shepherdviolet/turquoise/blob/master/doc/ExportImportManual.md <br/>

<br/>
<br/>
<br/>

# Module `turquoise`
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-thistle-dc143c.svg?style=flat)](https://github.com/shepherdviolet/thistle)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)

### Enhance ┃ sviolet.turquoise.enhance
###### sviolet.turquoise.enhance.app.TApplication/TActivity/TAppCompatActivity/TFragmentActivity/TFragment...
> Enhanced component
###### sviolet.turquoise.enhance.app.utils.InjectUtils
> Inject layout and views by `@ResourceId` annotation (Has been packaged in `TActivity`/`TAppCompatActivity`/`TFragmentActivity`)
###### sviolet.turquoise.enhance.app.utils.RuntimePermissionManager
> Easy to use runtime permission (Has been packaged in `TActivity`/`TAppCompatActivity`/`TFragmentActivity`)
###### sviolet.turquoise.enhance.common.*
> `WeakHandler/WeakAsyncTask` Used to avoid memory leaks.

### Kotlin ┃ sviolet.turquoise.kotlin
###### sviolet.turquoise.kotlin.extension
> Extension functions for android kotlin
###### sviolet.turquoise.kotlin.utilx.tfork.TFork
> Easy to coding async logic in kotlin

### Model ┃ sviolet.turquoise.model
###### sviolet.turquoise.model.cache.*
> Cache model for android
###### sviolet.turquoise.model.network.*
> Cookie jar for okhttp

### UI ┃ sviolet.turquoise.ui
###### sviolet.turquoise.ui.util.*
> Utils for view
###### sviolet.turquoise.ui.adapter.*
> `TViewHolder`/`TRecyclerViewHolder` is used to instead of manual coding `ViewHolder` in adapters<br/>
> Enhanced adapters for `RecyclerView`/`ViewPager`
###### sviolet.turquoise.ui.dialog.*
> Dialogs
###### sviolet.turquoise.ui.drawable.*
> Drawables (RoundedCornerBitmapDrawable/SafeBitmapDrawable)
###### sviolet.turquoise.ui.view.*
> Views (GifView/GradualImageView/RotateImageView/LinearShadowView/RotateTextView/ScrapeTextView...)
###### sviolet.turquoise.ui.viewgroup.*
> ViewGroups (animation/pull to refresh/tab view)

### UI suite ┃ sviolet.turquoise.uix
###### sviolet.turquoise.uix.slideengine.*
> Help to build sliding views (ViewGroup), and provides some ready-made Views
###### sviolet.turquoise.uix.viewgesturectrl.*
> Help to build views which controlled by gestures

### Util ┃ sviolet.turquoise.util
###### sviolet.turquoise.util.*
> Utils of android/bitmap/bluetooth/conversion/crypto/reflect...

### Util suite ┃ sviolet.turquoise.utilx
###### sviolet.turquoise.utilx.eventbus.EvBus
> Lightweight event bus to post message between components
###### sviolet.turquoise.utilx.lifecycle.LifeCycleUtils
> Used to bind listeners to the lifecycle of components
###### sviolet.turquoise.utilx.tlogger.TLogger
> Log utils

<br/>
<br/>
<br/>

# Module `turquoise-imageloader`
![API](https://img.shields.io/badge/API-14%2B-6a5acd.svg?style=flat)
![Dependency](https://img.shields.io/badge/Module%20Dependency-turquoise-2ed8a8.svg?style=flat)
![Dependency](https://img.shields.io/badge/Maven%20Dependency-support--v4-dc143c.svg?style=flat)
[![Dependency](https://img.shields.io/badge/Maven%20Dependency-android--gif--drawable-dc143c.svg?style=flat)](https://github.com/koral--/android-gif-drawable)

###### Load and display image from network

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

        //Load image and display in ImageView
        TILoader.node(this).load(url, params, imageView);

```

### Preview:
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_list.gif)
![](https://github.com/shepherdviolet/static-resources/blob/master/image/tiloader/tiloader_demo_rounded.gif)
![](https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/tiloader/tiloader_demo_gif.gif)<br/>
<br/>
<br/>
<br/>
