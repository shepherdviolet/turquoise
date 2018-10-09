package sviolet.demoaimageloader.demos;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import sviolet.demoaimageloader.R;
import sviolet.demoaimageloader.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.util.bitmap.BlurUtils;
import sviolet.turquoise.util.droid.DirectoryUtils;
import sviolet.turquoise.util.droid.MeasureUtils;
import sviolet.turquoise.x.common.tlogger.TLogger;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.TILoaderUtils;
import sviolet.turquoise.x.imageloader.drawable.common.CircleLoadingAnimationDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.common.CommonFailedDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.common.CommonLoadingDrawableFactory;
import sviolet.turquoise.x.imageloader.drawable.common.PointLoadingAnimationDrawableFactory;
import sviolet.turquoise.x.imageloader.entity.ImageResource;
import sviolet.turquoise.x.imageloader.entity.NodeSettings;
import sviolet.turquoise.x.imageloader.entity.OnLoadedListener;
import sviolet.turquoise.x.imageloader.entity.Params;
import sviolet.turquoise.x.imageloader.handler.DecodeHandler;
import sviolet.turquoise.x.imageloader.node.Task;
import sviolet.turquoise.x.imageloader.stub.StubRemoter;

/**
 * Basic Usage of TurquoiseImageLoader
 * <p/>
 * Created by S.Violet on 2016/3/8.
 */

@DemoDescription(
        title = "Basic Usage",
        type = "",
        info = "Basic Usage of TurquoiseImageLoader"
)

@ResourceId(R.layout.basic_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class BasicActivity extends TActivity {

    private static final int IMAGE_VIEW_6_EXTENSION_INDEX = 0;

    @ResourceId(R.id.basic_main_imageview1)
    private ImageView imageView1;
    @ResourceId(R.id.basic_main_imageview2)
    private ImageView imageView2;
    @ResourceId(R.id.basic_main_imageview3)
    private ImageView imageView3;
    @ResourceId(R.id.basic_main_imageview4)
    private ImageView imageView4;
    @ResourceId(R.id.basic_main_imageview5)
    private ImageView imageView5;
    @ResourceId(R.id.basic_main_imageview6)
    private ImageView imageView6;
    @ResourceId(R.id.basic_main_imageview7)
    private ImageView imageView7;
    @ResourceId(R.id.basic_main_imageview8)
    private ImageView imageView8;
    @ResourceId(R.id.basic_main_imageview9)
    private ImageView imageView9;
    @ResourceId(R.id.basic_main_imageview10)
    private ImageView imageView10;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        //必须在加载之前设置节点,否则无效
        initNode();
        loadImage();
        extractImage();
    }

    /**
     * 节点设置, 仅作用于该Context的加载任务. 节点设置与全局设置冲突时, 以节点设置为准.
     * 该方法必须在节点加载图片前执行, 否则设置无效, 建议在Activity.onCreate()中调用.
     */
    private void initNode() {
        /*
         * 节点请求队列长度: 当屏幕中同时出现(加载)的图片数量很多时, 需要适当调大队列长度. TILoader.extract()方法通过特殊节点加载, 该设置不影响.
         * 当你的界面中有大量的图片需要加载, 但总是会有几张图加载失败的情况, 就需要将该队列长度增大.
         * 以一个屏幕中最多可能同时出现(加载)20张图片的场合为例, 请求队列长度建议设置为30, 即1.5倍.
         */
        TILoader.node(this).setting(new NodeSettings.Builder()
//                .setRequestQueueSize(10)//节点请求队列长度
//                .setReloadTimes(2)//加载失败重试次数
//                .setNetworkConnectTimeout(3000)//网络连接超时ms
//                .setNetworkReadTimeout(3000)//网络读取超时ms
//                .setImageAppearDuration(400)//加载成功后, 图片渐渐出现动画的时间ms
//                .setNetworkLoadHandler(new MyNetworkLoadHandler())//自定义实现网络加载
//                .setBackgroundColor(0xFFF0F0F0)//自定义背景色(作为加载目标图的背景)
//                .setBackgroundImageResId(R.mipmap.async_image_loading)//自定义背景图(作为加载目标图的背景, 不常用)
////                .setLoadingDrawableFactory(new MyLoadingDrawableFactory())//方式1:自定义实现加载图(完全自己实现)
                .setLoadingDrawableFactory(new CommonLoadingDrawableFactory()//方式2:配置通用加载图
                        .setBackgroundColor(0xFFF0F0F0)//加载图背景颜色
                        .setImageResId(R.mipmap.async_image_loading)//加载图设置图片
                        .setImageScaleType(CommonLoadingDrawableFactory.ImageScaleType.FORCE_CENTER)//设置加载图拉伸方式为强制居中
                        .setAnimationEnabled(true)//允许动画(默认true)
//                        .setAnimationDrawableFactory(new MyAnimationDrawableFactory())//方式1:自定义实现动画(完全自己实现)
                        .setAnimationDrawableFactory(new CircleLoadingAnimationDrawableFactory()//方式2:配置通用动画
                                .setRotateStep(7)//旋转步进(速度)
                                .setRadius(0.10f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_WIDTH)//半径为控件宽度的10%
                                .setCircleColor(0x20000000)//背景圈颜色
                                .setCircleStrokeWidth(0.01f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_WIDTH)//背景圈宽度为控件宽度的1%
                                .setProgressColor(0x40000000)//进度圈颜色
                                .setProgressStrokeWidth(0.01f, CircleLoadingAnimationDrawableFactory.SizeUnit.PERCENT_OF_WIDTH)))//进度圈宽度为控件宽度的1%
////                .setFailedDrawableFactory(new MyFailedDrawableFactory())//方式1:自定义实现加载失败图
//                .setFailedDrawableFactory(new CommonFailedDrawableFactory()//方式2:配置通用失败图
//                        .setColor(0xFFB0B0B0)//失败图背景色
//                        .setImageResId(R.mipmap.async_image_loading))//设置失败图
                .setExtensionBackgroundColor(IMAGE_VIEW_6_EXTENSION_INDEX, 0xFF80FF80)//特殊背景色
                .setExtensionLoadingDrawableFactory(IMAGE_VIEW_6_EXTENSION_INDEX, new CommonLoadingDrawableFactory()//特殊加载图
                        .setBackgroundColor(0xFF80FF80)//加载背景色
                        .setAnimationDrawableFactory(new PointLoadingAnimationDrawableFactory()))//点样式的动画
                .setExtensionFailedDrawableFactory(IMAGE_VIEW_6_EXTENSION_INDEX, new CommonFailedDrawableFactory()//特殊失败图
                        .setColor(0xFFFF8080))//失败图颜色
                .build());
    }

    /**
     * 加载图片并显示在控件上, TILoader会自动管理图片(加载/重新加载/缓存/回收)
     */
    private void loadImage() {

        /*
         * 加载参数:
         *
         * 主要有两种加载模式::
         *
         * 1.尺寸匹配控件模式: 默认模式, 即不设置setReqSize(...)
         * 图片会被解码成接近控件尺寸的大小, 图片填充控件, 加载图和失败图也会填充控件.
         *
         * 2.指定尺寸模式: 通过setReqSize(...)设置
         * 图片会被解码成接近指定尺寸的大小, 图片显示尺寸等于指定尺寸, 加载图和失败图尺寸等于指定尺寸.
         *
         */
//        Params paramsDemo = new Params.Builder()
//                .setReqSize(100, 100)//指定加载尺寸, 默认匹配控件尺寸
//                .setBitmapConfig(Bitmap.Config.ARGB_8888)//设置Bitmap格式, ARGB_8888支持透明, 默认ARGB_565
//                .setDecodeInSampleQuality(BitmapUtils.InSampleQuality.MEDIUM)//解码采样质量为中, 若需要解码原图, 请设置ORIGINAL
//                .setDecodeScaleStrategy(DecodeHandler.DecodeScaleStrategy.NO_SCALE)//默认不缩放图片, 此参数可以缩放图片
//                .setDecodeInterceptor(new MyDecodeInterceptor())//设置解码拦截器, 对图片进行特殊解码处理
//                .build();

        /*
         * 适用于:
         * 控件尺寸固定的场合(即非wrap_content)
         *
         * TILoader默认根据控件尺寸加载图片, 图片会被解码成接近控件尺寸的大小, 图片填充控件,加载图和失败图也会填充控件.
         */
        String url1 = "https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/slate.jpg";
        TILoader.node(this).load(url1, imageView1);//默认根据控件尺寸加载图片

        imageView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 适用于:
         * 1.控件尺寸不固定(wrap_content)
         * 2.提前知道目标图片尺寸, 显示的图片根据目标图片尺寸变化
         * 3.动态设置加载/显示的图片尺寸
         *
         * 图片会被解码成接近指定尺寸的大小, 图片显示尺寸等于指定尺寸, 加载图和失败图尺寸等于指定尺寸.
         */
        String url2 = "https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/turquoise.jpg";
        int image2Width = MeasureUtils.getScreenWidth(this) / 3;//指定图片宽度
        int image2Height = image2Width * 3 / 4;//指定图片高度
        Params params2 = new Params.Builder()
                .setReqSize(image2Width, image2Height)//指定尺寸
                .build();
        TILoader.node(this).load(url2, imageView2, params2);

        imageView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 适用于:
         * 1.控件尺寸不固定(wrap_content)
         * 2.加载图有固定尺寸
         *
         * 图片会被解码成接近加载图的尺寸, 图片显示尺寸等于自身解码后的尺寸, 加载图和失败图尺寸等于加载图尺寸.
         */
        String url3="https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/crimson.jpg";
        TILoader.node(this).load(url3, imageView3);

        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 增加解码拦截器, 实现高斯模糊
         */
        String url5 = url2;
        int image5Width = MeasureUtils.getScreenWidth(this) / 3;//指定图片宽度
        int image5Height = image5Width * 3 / 4;//指定图片高度
        Params params5 = new Params.Builder()
                .setReqSize(image5Width, image5Height)//指定尺寸
                .setDecodeInterceptor(new DecodeHandler.Interceptor() {
                    @Override
                    public ImageResource intercept(Context applicationContext, Context context, Task.Info taskInfo, ImageResource imageResource, TLogger logger) {
                        if (imageResource.getType() == ImageResource.Type.BITMAP && imageResource.getResource() instanceof Bitmap){
                            return new ImageResource(ImageResource.Type.BITMAP, BlurUtils.blurByJava((Bitmap) imageResource.getResource(), 10, true));
                        }
                        return imageResource;
                    }
                })
                .build();
        TILoader.node(this).load(url5, imageView5, params5);

        imageView5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 配置不同的加载图/背景图/失败图, 可配置无数种
         */
        String url6 = url1;
        Params params6 = new Params.Builder()
                .useExtensionLoadingDrawableFactory(IMAGE_VIEW_6_EXTENSION_INDEX)//使用特殊的加载图
                .useExtensionFailedDrawableFactory(IMAGE_VIEW_6_EXTENSION_INDEX)//使用特殊的失败图
                .useExtensionBackgroundDrawableFactory(IMAGE_VIEW_6_EXTENSION_INDEX)//使用特殊的背景图
                .setImageAppearDuration(0)//设置图片出现的时间
                .build();
        TILoader.node(this).load(url6, imageView6, params6);

        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 从res加载图片
         */
        String url7 = "mipmap/rounded_list_image1";
        Params params7 = new Params.Builder()
                .setSourceType(Params.SourceType.APK_RES)
                .build();
        TILoader.node(this).load(url7, imageView7, params7);

        imageView7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 从res加载图片
         */
        String url8 = "images/local_image_1.jpg";
        Params params8 = new Params.Builder()
                .setSourceType(Params.SourceType.APK_ASSETS)
                .build();
        TILoader.node(this).load(url8, imageView8, params8);

        imageView8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 加载本地图片
         */
        String url9 = DirectoryUtils.getExternalStorageDirectory().getAbsolutePath() + "/test.jpg";
        Params params9 = new Params.Builder()
                .setSourceType(Params.SourceType.LOCAL_DISK)
                .build();
        TILoader.node(this).load(url9, imageView9, params9);

        imageView9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

        /*
         * 生成二维码
         */
        String url10 = "Hello TILoader";
        Params params10 = new Params.Builder()
                .setSourceType(Params.SourceType.URL_TO_QR_CODE)
                .addExtra(DecodeHandler.EXTRA_REQ_DIMENSION_ZOOM, 0.5f)//save memory
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_CHARSET, "utf-8")
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_MARGIN, 1)
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_CORRECTION_LEVEL, ZxingUtils.CorrectionLevel.M)
//                .addExtra(Params.EXTRA_URL_TO_QR_CODE_FORCE_SQUARE, false)
                .build();
        TILoader.node(this).load(url10, imageView10, params10);

        imageView10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                TILoaderUtils.getStubRemoter(v).relaunch();
            }
        });

    }

    /**
     * 下载图片, 获得的图片将不会由TILoader管理, 请自行处理.
     */
    private void extractImage(){

        final StubRemoter remoter = TILoader.extract(this, "https://raw.githubusercontent.com/shepherdviolet/static-resources/master/image/logo/cornflower.jpg", null, new OnLoadedListener<ImageView>() {
            @Override
            public void onLoadSucceed(String url, Params params, ImageResource resource) {
                //图片下载成功
                ImageView imageView = getWeakRegister();//获取弱引用持有的ImageView
                if (imageView != null) {
                    Toast.makeText(BasicActivity.this, "load succeed", Toast.LENGTH_SHORT).show();
                    //用TILoaderUtils的方法, 将ImageResource转为Drawable, 设置跳过绘制错误
                    imageView.setImageDrawable(TILoaderUtils.imageResourceToDrawable(BasicActivity.this, resource, true));
                }else{
                    //ImageView已被GC
                    TLogger.get(this).e("imageView is recycled, can't show image");
                }
            }
            @Override
            public void onLoadCanceled(String url, Params params) {
                //图片下载失败, 可尝试重新下载
                Toast.makeText(BasicActivity.this, "load failed", Toast.LENGTH_SHORT).show();
            }
        }.setWeakRegister(imageView4));//使用弱引用持有ImageView, 防止内存泄漏

        imageView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //失败重载
                remoter.relaunch();
            }
        });

    }

}
