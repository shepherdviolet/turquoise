package sviolet.demoa.image;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.utils.bitmap.BitmapUtils;
import sviolet.turquoise.utils.sys.MeasureUtils;
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
    }

    /**
     * 销毁资源<br/>
     * 自行创建的CachedBitmapUtils,BitmapCache,BitmapLoader,AsyncBitmapDrawableLoader需要在此销毁(destroy)<br/>
     * Activity自带的CachedBitmapUtils无需手动销毁, TActivity会自动销毁<Br/>
     * @see TActivity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
//        cachedBitmapUtils.destroy();//销毁示例
    }

    private final Handler handler = new Handler();

    /***************************************************************************************
     * 第一行:基础CachedBitmapUtils用法:解码
     ***************************************************************************************/

    @ResourceId(R.id.image_common_imageview11)
    private ImageView imageView11;
    @ResourceId(R.id.image_common_imageview12)
    private ImageView imageView12;

    private void initLine1(){

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

    private void initLine2(){

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
        bitmap23 = getCachedBitmapUtils().zoom(null, bitmap23, 0.4f);
        imageView23.setImageBitmap(bitmap23);

        /**
         * 其他转换/编辑功能
         */

//        getCachedBitmapUtils().bitmapToBase64()//将Bitmap转为Base64数据

    }

    /***************************************************************************************
     * 第三行:基础AsyncBitmapDrawable用法
     ***************************************************************************************/

    @ResourceId(R.id.image_common_imageview31)
    private ImageView imageView31;
    @ResourceId(R.id.image_common_imageview32)
    private ImageView imageView32;

    private void initLine3(){

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

}
