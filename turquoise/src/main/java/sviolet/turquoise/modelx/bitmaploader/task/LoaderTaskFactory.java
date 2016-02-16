package sviolet.turquoise.modelx.bitmaploader.task;

import android.view.View;

import sviolet.turquoise.modelx.bitmaploader.SimpleBitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.SimpleBitmapLoaderTask;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;

/**
 * 图片加载任务工厂(接口)
 *
 * Created by S.Violet on 2016/2/16.
 */
public interface LoaderTaskFactory {

    /**
     * 创建控件图片加载任务
     */
    SimpleBitmapLoaderTask newLoaderTask(BitmapRequest request, SimpleBitmapLoader loader, View view);

    /**
     * 创建控件背景图片加载任务
     */
    SimpleBitmapLoaderTask newBackgroundLoaderTask(BitmapRequest request, SimpleBitmapLoader loader, View view);

}
