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

package sviolet.demoa.image;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.bitmap.loader.BitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.OnBitmapLoadedListener;
import sviolet.turquoise.utils.bitmap.loader.SimpleBitmapLoader;
import sviolet.turquoise.utils.bitmap.loader.handler.DefaultNetLoadHandler;
import sviolet.turquoise.utils.sys.MeasureUtils;
import sviolet.turquoise.utils.sys.NetStateUtils;
import sviolet.turquoise.view.drawable.TransitionBitmapDrawable;

@DemoDescription(
        title = "Common Image Demo",
        type = "Image",
        info = "an Simple Image Demo"
)

/**
 * 简易的图片加载Demo<br/>
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.image_common)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class CommonImageActivity extends TActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getCachedBitmapUtils(0.1f, 0.1f);//初始化Activity自带的CachedBitmapUtils, 设置内存占用比

        initLine1();
        initLine2();
        initLine3();
        initLine4();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Activity不再显示时, 压缩图片缓存占用空间, 非必须, 在内存紧张场合适用
        if (bitmapLoader != null)
            bitmapLoader.reduce();
        if (simpleBitmapLoader != null)
            simpleBitmapLoader.reduce();
    }

    /**
     * 销毁资源<br/>
     * 自行创建的CachedBitmapUtils,BitmapCache,BitmapLoader,AsyncBitmapDrawableLoader需要在此销毁(destroy)<br/>
     * Activity自带的CachedBitmapUtils无需手动销毁, TActivity会自动销毁<Br/>
     * {@link TActivity}
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cachedBitmapUtils.destroy();//销毁示例

        if (bitmapLoader != null)
            bitmapLoader.destroy();
        if (simpleBitmapLoader != null)
            simpleBitmapLoader.destroy();
    }

    private final Handler handler = new Handler();

    /***************************************************************************************
     * 第一行:基础CachedBitmapUtils用法:解码
     ***************************************************************************************/

    @ResourceId(R.id.image_common_imageview11)
    private ImageView imageView11;
    @ResourceId(R.id.image_common_imageview12)
    private ImageView imageView12;

    private void initLine1() {

        /**
         * res资源解码为图片<br/>
         * 注意::<Br/>
         * reqWidth/reqHeight参数只用于适当缩小图片, 节省内存开销, 解码出的图尺寸不等于reqWidth/reqHeight<br/>
         */

        imageView11.setImageBitmap(
                getCachedBitmapUtils().decodeFromResource(
                        null,//此处送空, CachedBitmapUtils会自动分配一个不重复的key
                        getResources(),
                        R.mipmap.async_image_1,//资源ID
                        MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                        MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
                )
        );
        imageView12.setImageBitmap(
                getCachedBitmapUtils().decodeFromResource(
                        "imageView12",//指定缓存中的key, 后续可以单独对指定图片操作, unused(key)/get(key)等
                        getResources(),
                        R.mipmap.async_image_2,//资源ID
                        MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                        MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
                )
        );

        /**
         * 其他解码方法
         */

//        getCachedBitmapUtils().decodeFromBase64()//Base64数据解码为图片
//        getCachedBitmapUtils().decodeFromByteArray()//二进制数据解码为图片
//        getCachedBitmapUtils().decodeFromFile()//文件解码为图片
//        getCachedBitmapUtils().decodeFromStream()//输入流解码为图片,一个流只能被解码一次

    }

    /***************************************************************************************
     * 第二行:基础CachedBitmapUtils用法:转换/编辑
     ***************************************************************************************/

    @ResourceId(R.id.image_common_imageview21)
    private ImageView imageView21;
    @ResourceId(R.id.image_common_imageview22)
    private ImageView imageView22;
    @ResourceId(R.id.image_common_imageview23)
    private ImageView imageView23;

    private void initLine2() {

        /**
         * 绘制文字
         */

        //先用BitmapUtils解码(无缓存)
        Bitmap bitmap21 = BitmapUtils.decodeFromResource(
                getResources(),
                R.mipmap.async_image_1,//资源ID
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
        );
        //在图片上绘制文字
        //CachedBitmapUtils.drawText会将原Bitmap回收
        bitmap21 = getCachedBitmapUtils().drawText(
                null,
                bitmap21, //原图
                "文字", //文字
                MeasureUtils.dp2px(getApplicationContext(), 4), //文字距离图片左边的距离 4dp
                MeasureUtils.dp2px(getApplicationContext(), 40), //文字底边距离图片上边的距离 40dp
                50f, //字体大小
                0xFF000000 //字体颜色
        );
        imageView21.setImageBitmap(bitmap21);

        /**
         * 圆角处理
         */

        //先用BitmapUtils解码(无缓存)
        Bitmap bitmap22 = BitmapUtils.decodeFromResource(
                getResources(),
                R.mipmap.async_image_2,//资源ID
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
        );
        //下边两个角做圆角处理
        //CachedBitmapUtils.drawText会将原Bitmap回收
        bitmap22 = getCachedBitmapUtils().toRoundedCorner(null, bitmap22, 50f, BitmapUtils.RoundedCornerType.TopLeft_And_BottomLeft);
        imageView22.setImageBitmap(bitmap22);

        /**
         * 缩放图片
         */

        //先用BitmapUtils解码(无缓存)
        Bitmap bitmap23 = BitmapUtils.decodeFromResource(
                getResources(),
                R.mipmap.async_image_3,//资源ID
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
        );
        //缩小Bitmap
        //CachedBitmapUtils.zoom会将原Bitmap回收
        bitmap23 = getCachedBitmapUtils().scale(null, bitmap23, 0.4f);
        imageView23.setImageBitmap(bitmap23);

        /**
         * 其他转换/编辑功能
         */

//        getCachedBitmapUtils().bitmapToBase64()//将Bitmap转为Base64数据

    }

    /***************************************************************************************
     * 第三行:基础TransitionBitmapDrawable用法(慎用)
     ***************************************************************************************/

    @ResourceId(R.id.image_common_imageview31)
    private ImageView imageView31;
    @ResourceId(R.id.image_common_imageview32)
    private ImageView imageView32;

    private void initLine3() {

        /**
         * TransitionBitmapDrawable用法
         * 优点:
         * 1.即使图片被回收, 也不会抛异常, 而是显示默认图
         * 2.即使默认图被回收, 也不会抛异常, 而是显示空图
         * 3.淡入效果
         */

        //默认图
        //一张默认图可以同时给多个AsyncBitmapDrawable使用
        //默认图需要手动回收, 但由于使用Activity自带的CachedBitmapUtils, 因此会自动回收
        final Bitmap defaultBitmap3 = getCachedBitmapUtils().decodeFromResource(
                null,
                getResources(),
                R.mipmap.async_image_null,//资源ID
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
        );

        //显示的图1
        final Bitmap bitmap31 = getCachedBitmapUtils().decodeFromResource(
                null,
                getResources(),
                R.mipmap.async_image_1,//资源ID
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
        );
        //显示的图2
        final Bitmap bitmap32 = getCachedBitmapUtils().decodeFromResource(
                null,
                getResources(),
                R.mipmap.async_image_2,//资源ID
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100)//需求100dp(实际高不等于100dp)
        );
        //设置Drawable
        imageView31.setImageDrawable(
                //异步BitmapDrawable
                new TransitionBitmapDrawable(
                        getResources(),
                        defaultBitmap3 //默认图, 可设置为null
                ).setBitmap(
                        getResources(),
                        bitmap31, //显示的图
                        1500 //淡入动画效果持续1500ms
                )
        );
        //设置Drawable
        imageView32.setImageDrawable(
                //异步BitmapDrawable
                new TransitionBitmapDrawable(
                        getResources(),
                        defaultBitmap3 //默认图, 可设置为null
                ).setBitmap(
                        getResources(),
                        bitmap32, //显示的图
                        1500 //淡入动画效果持续1500ms
                )
        );
        /**
         * 为模拟图片被意外回收的情况, 采用延时的方式, 先后把Bitmap回收掉
         */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bitmap31.recycle();
                imageView31.postInvalidate();
            }
        }, 1000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bitmap32.recycle();
                imageView32.postInvalidate();
            }
        }, 2000);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                defaultBitmap3.recycle();
                imageView31.postInvalidate();
                imageView32.postInvalidate();
            }
        }, 3000);
    }

    /***************************************************************************************
     * 第四行:基础BitmapLoader用法
     ***************************************************************************************/

    private BitmapLoader bitmapLoader;
    private SimpleBitmapLoader simpleBitmapLoader;

    @ResourceId(R.id.image_common_imageview41)
    private ImageView imageView41;
    @ResourceId(R.id.image_common_imageview42)
    private ImageView imageView42;

    private void initLine4() {

        //仅在wifi下显示
        if (!NetStateUtils.isWifi(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "第四行图片请在wifi下查看", Toast.LENGTH_SHORT).show();
            return;
        }

        /**
         * 创建BitmapLoader实例, 配置并启动.
         *
         * destroy::
         * 务必在onDestroy时销毁实例, bitmapLoader.destroy().
         *
         * unused::
         * 由于该示例中, 图片始终显示在屏幕中, 不存在"废弃"图片, 因此没有使用
         * loader.unused()方法废弃图片, 若实际情况中, 明确图片不再需要显示,
         * 务必调用loader.unused()方法, 废弃图片, 便于资源回收
         */

        bitmapLoader = new BitmapLoader(this, "AsyncImageActivity")
//                .setNetLoadHandler(new DefaultNetLoadHandler(10000, 30000, true))//设置超时时间, 也可以自定义实现网络加载
                .setRamCache(0.1f, 0.1f)//缓存和回收站各占10%内存
                .setDiskCache(50, 5, 10)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量10
                .setNetLoad(3, 10)//3线程网络加载, 等待队列容量10
//                .setDiskCacheInner()//强制使用内部储存
//                .setWipeOnNewVersion()//当APP更新时清空磁盘缓存
                .setLogger(getLogger());//打印日志
        bitmapLoader.open();//启动(必须)

        /**
         * BitmapLoader普通方式加载图片
         */

        //图片url
        String url41 = "https://avatars0.githubusercontent.com/u/12589661?v=3&s=460";

        /*
            利用View.setTag, 在控件上记录加载信息, url便于后续BitmapLoader.unused(), reloadTimes用于
            限制重新加载次数
         */
        imageView41.setTag(new TaskInfo(url41));

        //加载图片
        bitmapLoader.load(
                url41,//URL
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际高不等于100dp)
                imageView41,//控件作为参数传入, 便于监听器中设置图片
                mOnBitmapLoadedListener41 //结果监听器
        );

//        bitmapLoader.unused(url41);//弃用Bitmap, 可回收资源/取消加载任务

        /**
         * 创建SimpleBitmapLoader实例, 配置并启动.
         *
         * destroy::
         * 务必在onDestroy时销毁实例, bitmapLoader.destroy().
         *
         * unused::
         * 由于该示例中, 图片始终显示在屏幕中, 不存在"废弃"图片, 因此没有使用
         * loader.unused()方法废弃图片, 若实际情况中, 明确图片不再需要显示,
         * 务必调用loader.unused()方法, 废弃图片, 便于资源回收
         */

        simpleBitmapLoader = new SimpleBitmapLoader(this, "AsyncImageActivity",
                BitmapUtils.decodeFromResource(getResources(), R.mipmap.async_image_null))
//                .setNetLoadHandler(new DefaultNetLoadHandler(10000, 30000, true))//设置超时时间, 也可以自定义实现网络加载
                .setRamCache(0.1f, 0.1f)//缓存和回收站各占15%内存
//                    .setRamCache(0.004f, 0.004f)//测试:即使内存不足,显示的Bitmap被回收, 也不会抛异常
                .setDiskCache(50, 5, 10)//磁盘缓存50M, 5线程磁盘加载, 等待队列容量10
                .setNetLoad(3, 10)//3线程网络加载, 等待队列容量10
//                    .setDiskCacheInner()//强制使用内部储存
//                    .setWipeOnNewVersion()//当APP更新时清空磁盘缓存
                .setLogger(getLogger())//打印日志
                .setAnimationDuration(400)//设置图片淡入动画持续时间400ms
                .setReloadTimesMax(2);//设置图片加载失败重新加载次数限制
        simpleBitmapLoader.open();//启动(必须)

        /**
         * SimpleBitmapLoader加载图片
         *
         * 特别注意:用SimpleBitmapLoader加载的控件, 禁止使用View.setTag(),
         * 例如此处ImageView, 不要使用ImageView.setTag(), 这样会使加载失败.
         * 因为SimpleBitmapLoader将回调对象设置在View的tag里.
         */

        String url42 = "https://avatars1.githubusercontent.com/u/13911857?v=3&s=460";

        simpleBitmapLoader.load(
                url42,//URL
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际宽不等于100dp)
                MeasureUtils.dp2px(getApplicationContext(), 100),//需求100dp(实际高不等于100dp)
                imageView42//被加载控件
        );

//        simpleBitmapLoader.unused(imageView42);//弃用Bitmap, 可回收资源/取消加载任务

        //测试简易防回收崩溃
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                simpleBitmapLoader.destroy();
//                imageView42.postInvalidate();
//            }
//        }, 10000);

    }

    /**
     * imageView41<br/>
     * 图片加载结束监听器<br/>
     */
    private OnBitmapLoadedListener mOnBitmapLoadedListener41 = new OnBitmapLoadedListener() {
        @Override
        public void onLoadSucceed(String url, int reqWidth, int reqHeight, Object params, Bitmap bitmap) {
            //加载成功
            getLogger().i("[demoa]41 load succeed");

            if (bitmap != null && !bitmap.isRecycled()) {
                //获得传入的控件
                ImageView imageView = (ImageView) params;
                //利用TransitionDrawable增加显示效果
                TransitionDrawable drawable = new TransitionDrawable(new Drawable[]{new ColorDrawable(0x00000000), new BitmapDrawable(getResources(), bitmap)});
                //设置图片
                imageView.setImageDrawable(drawable);
                //开始动画
                drawable.startTransition(500);
            }
        }

        @Override
        public void onLoadFailed(String url, int reqWidth, int reqHeight, Object params) {
            //加载失败, 尝试重新加载
            getLogger().i("[demoa]41 load failed");
            //获得传入的控件
            ImageView imageView = (ImageView) params;
            //获得控件tag中的参数
            TaskInfo taskInfo = (TaskInfo) imageView.getTag();

            //根据TaskInfo记录的重新加载次数, 判断是否重新加载
            if (taskInfo.reloadTimes < TaskInfo.RELOAD_TIMES_MAX) {
                getLogger().i("[demoa]41 reload");
                //计数+1
                taskInfo.reloadTimes++;
                //重新加载
                bitmapLoader.load(url, reqWidth, reqHeight, params, this);
            }
        }

        @Override
        public void onLoadCanceled(String url, int reqWidth, int reqHeight, Object params) {
            //取消通常不用处理
            getLogger().i("[demoa]41 load canceled");
        }
    };

    /**
     * imageView41<br/>
     * 用于BitmapLoader普通加载方式, 记录控件的加载任务信息<br/>
     */
    private class TaskInfo {
        static final int RELOAD_TIMES_MAX = 2;//最大重加载次数
        String url;//加载地址
        int reloadTimes;//重新加载次数

        TaskInfo(String url) {
            this.url = url;
        }

    }

}
