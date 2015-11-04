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
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.utils.bitmap.loader.AsyncBitmapDrawable;
import sviolet.turquoise.utils.bitmap.loader.AsyncBitmapDrawableLoader;
import sviolet.turquoise.utils.sys.MeasureUtils;

/**
 * ListView适配器
 * Created by S.Violet on 2015/7/7.
 */
public class AsyncImageAdapter2 extends BaseAdapter {

    private Context context;
    private List<AsyncImageItem> itemList;
    private AsyncBitmapDrawableLoader asyncBitmapDrawableLoader;
    private int widthHeightLarge, widthHeightSmall;

    /**
     * @param context context
     * @param itemList 数据
     * @param asyncBitmapDrawableLoader 用于图片动态加载缓存
     */
    public AsyncImageAdapter2(Context context, List<AsyncImageItem> itemList, AsyncBitmapDrawableLoader asyncBitmapDrawableLoader){
        this.context = context;
        this.itemList = itemList;
        this.asyncBitmapDrawableLoader = asyncBitmapDrawableLoader;

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
        View view = convertView;
        ViewHolder holder;
        if (view == null){
            view = View.inflate(context, R.layout.image_async2_item, null);
            holder = new ViewHolder();
            holder.imageView[0] = (ImageView) view.findViewById(R.id.image_async_item_imageview0);
            holder.imageView[1] = (ImageView) view.findViewById(R.id.image_async_item_imageview1);
            holder.imageView[2] = (ImageView) view.findViewById(R.id.image_async_item_imageview2);
            holder.imageView[3] = (ImageView) view.findViewById(R.id.image_async_item_imageview3);
            holder.imageView[4] = (ImageView) view.findViewById(R.id.image_async_item_imageview4);
            holder.titleTextView = (TextView) view.findViewById(R.id.image_async_item_title);
            holder.contentTextView = (TextView) view.findViewById(R.id.image_async_item_content);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        AsyncImageItem item = itemList.get(position);
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());

        for (int i = 0 ; i < 5 ; i++) {
            /**
             * [重要]必须在设置新图前, 将原来的AsyncBitmapDrawable置为不再使用, 这样能取消原图的加载任务,
             * 保证需要显示的图能尽快加载出来, 使得滑动过程中的加载任务不占用等待队列, 防止滚动停止
             * 后界面中有图片未加载<br/>
             */
            AsyncBitmapDrawable drawable = (AsyncBitmapDrawable) holder.imageView[i].getDrawable();
            if (drawable != null)
                drawable.unused();

            /**
             * asyncBitmapDrawableLoader.load()方法返回的AsyncBitmapDrawable直接赋给ImageView
             */
            if (i == 0) {
                //第一张图为160*160dp, 其余80*80dp
                //加载成功逐渐显示动画效果
                holder.imageView[i].setImageDrawable(asyncBitmapDrawableLoader.load(item.getUrl(i), widthHeightLarge, widthHeightLarge));
            } else {
                holder.imageView[i].setImageDrawable(asyncBitmapDrawableLoader.load(item.getUrl(i), widthHeightSmall, widthHeightSmall));
            }

        }
        return view;
    }

    private class ViewHolder{
        TextView titleTextView;
        TextView contentTextView;
        ImageView[] imageView = new ImageView[5];
    }

}
