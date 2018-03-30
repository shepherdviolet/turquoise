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

import sviolet.demoaimageloader.custom.MyStubFactory;
import sviolet.turquoise.enhance.app.TApplication;
import sviolet.turquoise.enhance.app.annotation.setting.ApplicationSettings;
import sviolet.turquoise.enhance.app.annotation.setting.DebugSettings;
import sviolet.turquoise.enhance.app.annotation.setting.ReleaseSettings;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.entity.LowNetworkSpeedStrategy;
import sviolet.turquoise.x.imageloader.entity.ServerSettings;
import sviolet.turquoise.x.imageloader.handler.common.MultiThreadNetworkLoadHandler;

@ApplicationSettings(
        DEBUG = BuildConfig._DEBUG,//Debug模式, 装载DebugSetting配置
        transmitPipeLine = true//启用EvBus:Transmit模式
)
//发布配置
@ReleaseSettings(
        enableStrictMode = false,
        enableCrashRestart = true,
        enableCrashHandle = true,
        logGlobalLevel = TLogger.ERROR | TLogger.INFO
)
//调试配置
@DebugSettings(
        enableStrictMode = true,
        enableCrashRestart = false,
        enableCrashHandle = true,
        logGlobalLevel = TLogger.ERROR | TLogger.INFO | TLogger.WARNING | TLogger.DEBUG
)
public class MyApplication extends TApplication {

    @Override
    public void afterCreate() {
        super.afterCreate();
        initTILoader();
    }

    /**
     * TILoader全局配置, 建议在Application.onCreate()方法中执行. 该方法必须在TILoader初始化前执行,
     * 使用TILoader加载图片或使用TILoaderUtils均会初始化TILoader.
     */
    private void initTILoader() {

        /**
         * 高级配置:
         * @see LowNetworkSpeedStrategy
         */
//        LowNetworkSpeedStrategy lowNetworkSpeedStrategy = new LowNetworkSpeedStrategy.Builder()
//                .setConfigure(LowNetworkSpeedStrategy.Type.LOW_SPEED_MOBILE_NETWORK, 20000, 60000, 5 * 1024)
//                .setConfigure(LowNetworkSpeedStrategy.Type.HIGH_SPEED_MOBILE_NETWORK, 10000, 30000, 20 * 1024)
//                .setConfigure(LowNetworkSpeedStrategy.Type.WIFI_NETWORK, 10000, 30000, 20 * 1024)
//                .setConfigure(LowNetworkSpeedStrategy.Type.INDISPENSABLE_TASK, 40000, 120000, 256)
//                .build();

        TILoader.setting(new ServerSettings.Builder()
                .setLogEnabled(true)//允许日志打印
                .setMemoryCachePercent(getApplicationContext(), 0.1f)//分配10%的APP内存用于图片缓存
                .setDiskCacheSize(30)//分配30M用于图片磁盘缓存
                .setCustomStubFactory(new MyStubFactory())//自定义实现Stub工厂(用于增加对新控件的支持)
                .setNetworkLoadHandler(new MultiThreadNetworkLoadHandler(getApplicationContext(), 4, true))//改用多线程
//                .setDiskCachePath(getApplicationContext(), ServerSettings.DiskCachePath.EXTERNAL_STORAGE, "TILoaderDemo")//设置优先使用外部存储作为磁盘缓存, 子目录"TILoaderDemo"
//                .setMemoryLoadMaxThread(1)//设置内存加载线程数(默认1, 通常无需修改)
//                .setDiskLoadMaxThread(2)//设置磁盘加载线程数(默认2, 通常无需修改)
//                .setNetworkLoadMaxThread(3)//设置网络加载线程数(默认3, 通常无需修改)
//                .setNetworkConnectTimeout(3000)//设置网络加载连接超时
//                .setNetworkReadTimeout(7000)//设置网络加载读取超时
//                .setExceptionHandler(new MyExceptionHandler())//自定义实现异常处理
//                .setBackgroundColor(0xFFF0F0F0)//自定义背景色(作为加载目标图的背景)
//                .setBackgroundImageResId(R.mipmap.async_image_loading)//自定义背景图(作为加载目标图的背景, 不常用)
//                .setWipeDiskCacheWhenUpdate(true)//当APP更新时清空磁盘缓存(versionCode变化)
//                .setImageDataLengthLimitPercent(this, 0.3f)//高级配置:图片资源数据长度限制(超过设定值将取消加载任务)
//                .setLowNetworkSpeedStrategy(lowNetworkSpeedStrategy)//高级配置
////                .setLoadingDrawableFactory(new MyLoadingDrawableFactory())//方式1:自定义实现加载图(完全自己实现)
//                .setLoadingDrawableFactory(new CommonLoadingDrawableFactory()//方式2:配置通用加载图
//                        .setBackgroundColor(0xFFF0F0F0)//加载图背景颜色
//                        .setImageResId(R.mipmap.async_image_loading)//加载图设置图片
//                        .setImageScaleType(CommonLoadingDrawableFactory.ImageScaleType.FORCE_CENTER)//设置加载图拉伸方式为强制居中
//                        .setAnimationEnabled(true)//允许动画(默认true)
////                        .setAnimationDrawableFactory(new MyAnimationDrawableFactory())//方式1:自定义实现动画(完全自己实现)
//                        .setAnimationDrawableFactory(new CircleLoadingAnimationDrawableFactory()//方式2:配置通用动画
//                                .setRotateStep(7)//旋转步进(速度)
//                                .setRadius(0.15f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_WIDTH)//半径为控件宽度的15%
//                                .setCircleColor(0x20000000)//背景圈颜色
//                                .setCircleStrokeWidth(0.012f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_WIDTH)//背景圈宽度为控件宽度的1.2%
//                                .setProgressColor(0x40000000)//进度圈颜色
//                                .setProgressStrokeWidth(0.018f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_WIDTH)))//进度圈宽度为控件宽度的1.5%
////                .setFailedDrawableFactory(new MyFailedDrawableFactory())//方式1:自定义实现加载失败图
//                .setFailedDrawableFactory(new CommonFailedDrawableFactory()//方式2:配置通用失败图
//                        .setColor(0xFFB0B0B0)//失败图背景色
//                        .setImageResId(R.mipmap.async_image_loading))//设置失败图
                .build());

        /*
            设置TILoader日志级别, 对自定义的TLogger模块无效.
         */
//        TILoaderUtils.setLoggerLevel(TLogger.ERROR | TLogger.INFO);//仅打印ERROR和INFO日志(不打印DEBUG和WARNING日志)

    }

    @Override
    public void onUncaughtException(Throwable ex, boolean isCrashRestart) {
        //异常处理
    }

}
