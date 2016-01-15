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

package sviolet.demoa.image.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.modelx.bitmaploader.SimpleBitmapLoader;
import sviolet.turquoise.modelx.bitmaploader.entity.BitmapRequest;
import sviolet.turquoise.ui.common.ViewHolder;
import sviolet.turquoise.util.droid.MeasureUtils;

/**
 * ListView适配器
 * Created by S.Violet on 2015/7/7.
 */
public class AsyncImageAdapter2 extends BaseAdapter {

    private Context context;
    private List<AsyncImageItem> itemList;
    private SimpleBitmapLoader simpleBitmapLoader;
    private int widthHeightLarge, widthHeightSmall;

    /**
     * @param context context
     * @param itemList 数据
     * @param simpleBitmapLoader 用于图片动态加载缓存
     */
    public AsyncImageAdapter2(Context context, List<AsyncImageItem> itemList, SimpleBitmapLoader simpleBitmapLoader){
        this.context = context;
        this.itemList = itemList;
        this.simpleBitmapLoader = simpleBitmapLoader;

        //图片大小尺寸的长宽值
        widthHeightLarge = MeasureUtils.dp2px(context, 160);//160dp*160dp
        widthHeightSmall = MeasureUtils.dp2px(context, 80);//80dp*80dp
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = ViewHolder.create(context, convertView, parent, R.layout.image_async2_item);

        AsyncImageItem item = itemList.get(position);
        ((TextView) holder.get(R.id.image_async_item_title)).setText(item.getTitle());
        ((TextView) holder.get(R.id.image_async_item_content)).setText(item.getContent());

        ImageView[] images = new ImageView[5];
        images[0] = (ImageView) holder.get(R.id.image_async_item_imageview0);
        images[1] = (ImageView) holder.get(R.id.image_async_item_imageview1);
        images[2] = (ImageView) holder.get(R.id.image_async_item_imageview2);
        images[3] = (ImageView) holder.get(R.id.image_async_item_imageview3);
        images[4] = (ImageView) holder.get(R.id.image_async_item_imageview4);

        for (int i = 0 ; i < 5 ; i++) {
            /**
             * 废弃原有图片, 取消加载任务
             *
             * 通常情况下需要及时调用该方法废弃图片, 以取消加载任务, 回收内存.
             * 但ListView控件复用, simpleBitmapLoader.load方法会将View原来的加载任务废弃,
             * 因此ListView中可省略unused.
             */
//            simpleBitmapLoader.unused(holder.imageView[i]);

            /**
             * 加载图片
             */
            if (i == 0) {
                //第一张图为160*160dp, 其余80*80dp
                //加载成功逐渐显示动画效果
                simpleBitmapLoader.load(
                        new BitmapRequest(item.getUrl(i))//URL
                            .setReqDimension(widthHeightLarge, widthHeightLarge)//请求尺寸
                            .setBitmapConfig(Bitmap.Config.RGB_565), //设置颜色深度为RGB_565可节省内存,但不支持透明
                        images[i]);
            } else {
                simpleBitmapLoader.load(
                        new BitmapRequest(item.getUrl(i))//URL
                                .setReqDimension(widthHeightSmall, widthHeightSmall)//请求尺寸
                                .setBitmapConfig(Bitmap.Config.RGB_565), //设置颜色深度为RGB_565可节省内存,但不支持透明
                        images[i]);
            }

        }

        return holder.getConvertView();
    }

}
