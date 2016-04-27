/*
 * Copyright (C) 2015 S.Violet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.demoaimageloader;

import sviolet.demoaimageloader.common.Constants;
import sviolet.turquoise.enhance.app.TApplication;
import sviolet.turquoise.enhance.app.annotation.setting.ApplicationSettings;
import sviolet.turquoise.enhance.app.annotation.setting.DebugSettings;
import sviolet.turquoise.enhance.app.annotation.setting.ReleaseSettings;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;

@ApplicationSettings(
        DEBUG = BuildConfig._DEBUG //Debug模式, 装载DebugSetting配置
)
//发布配置
@ReleaseSettings(
        enableStrictMode = false,
        enableCrashRestart = true,
        enableCrashHandle = true,
        logDefaultTag = Constants.TAG,
        logGlobalLevel = TLogger.ERROR | TLogger.INFO
)
//调试配置
@DebugSettings(
        enableStrictMode = true,
        enableCrashRestart = false,
        enableCrashHandle = true,
        logDefaultTag = Constants.TAG,
        logGlobalLevel = TLogger.ERROR | TLogger.INFO | TLogger.WARNING | TLogger.DEBUG
)
public class MyApplication extends TApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        /*
            TILoader全局配置, 建议在Application.onCreate()方法中执行. 该方法必须在TILoader初始化前执行,
            使用TILoader加载图片或使用TILoaderUtils均会初始化TILoader.
         */
        TILoader.setting(new ServerSettings.Builder()
                .setMemoryCachePercent(getApplicationContext(), 0.1f)//分配10%的APP内存用于图片缓存
                .setDiskCacheSize(10)//分配10M用于图片磁盘缓存
//                .setDiskCachePath(getApplicationContext(), ServerSettings.DiskCachePath.EXTERNAL_STORAGE, "TILoaderDemo")//设置优先使用外部存储作为磁盘缓存, 子目录"TILoaderDemo"
//                .setMemoryLoadMaxThread(1)//设置内存加载线程数(默认1, 通常无需修改)
//                .setDiskLoadMaxThread(2)//设置磁盘加载线程数(默认2, 通常无需修改)
//                .setNetworkLoadMaxThread(3)//设置网络加载线程数(默认3, 通常无需修改)
//                .setNetworkConnectTimeout(3000)//设置网络加载连接超时
//                .setNetworkReadTimeout(3000)//设置网络加载读取超时
//                .setNetworkLoadHandler(new MyNetworkLoadHandler())//自定义实现网络加载
//                .setDecodeHandler(new MyDecodeHandler())//自定义实现图片解码
//                .setExceptionHandler(new MyExceptionHandler())//自定义实现异常处理
//                .setCustomStubFactory(new MyStubFactory())//自定义实现Stub工厂(用于增加对新控件的支持)
//                .setLoadingDrawableFactory(new MyLoadingDrawableFactory())//自定义实现加载图
//                .setFailedDrawableFactory(new MyFailedDrawableFactory())//自定义实现加载失败图
//                .setBackgroundColor(0xFFF0F0F0)//自定义背景色(作为加载目标图的背景)
//                .setBackgroundImageResId(R.mipmap.async_image_null)//自定义背景图(作为加载目标图的背景, 不常用)
//                .setWipeDiskCacheWhenUpdate(true)//当APP更新时清空磁盘缓存(versionCode变化)
                .setLogEnabled(true)//允许日志打印
                .build());

        /*
            设置TILoader日志级别, 对自定义的TLogger模块无效.
         */
//        TILoaderUtils.setLoggerLevel(TLogger.ERROR | TLogger.INFO);//仅打印ERROR和INFO日志(不打印DEBUG和WARNING日志)
    }

    @Override
    public void onUncaughtException(Throwable ex, boolean isCrashRestart) {
        //TODO 异常处理
    }

}
