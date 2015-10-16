package sviolet.turquoise.utils.bitmap.loader.enhanced;

import android.graphics.Bitmap;

import sviolet.turquoise.utils.bitmap.loader.OnBitmapLoadedListener;

/**
 * BitmapLoader加载任务处理器<br/>
 * 实现OnBitmapLoadedListener接口<br/>
 * <br/>
 * 不支持"防回收崩溃", 需配合BitmapLoader回收站使用.<br/>
 * <br/>
 * ----已实现------------------------------------<br/>
 * <br/>
 * 1.[重要]提供unused()方法,用于废弃图片,可回收资源/取消加载任务<br/>
 * 2.利用View.setTag()在控件中记录参数<br/>
 * 3.加载成功设置图片,支持淡入效果<br/>
 * 4.加载失败重新加载,含次数限制<br/>
 *
 * <br/>
 * ----待实现------------------------------------<br/>
 * 1.控件设置图片<br/>
 *
 * Created by S.Violet on 2015/10/16.
 */
public abstract class BitmapLoaderTaskHandler implements OnBitmapLoadedListener {

    @Override
    public void onLoadSucceed(String url, int reqWidth, int reqHeight, Object params, Bitmap bitmap) {

    }

    @Override
    public void onLoadFailed(String url, int reqWidth, int reqHeight, Object params) {

    }

    @Override
    public void onLoadCanceled(String url, int reqWidth, int reqHeight, Object params) {

    }

    private class TaskInfo{
        String url;
        int reloadTimes;
    }

}
