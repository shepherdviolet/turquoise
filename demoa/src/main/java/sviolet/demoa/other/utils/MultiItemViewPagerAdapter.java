/*
 * Copyright (C) 2015-2016 S.Violet
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

package sviolet.demoa.other.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.ui.adapter.RecyclingPagerAdapter;
import sviolet.turquoise.ui.adapter.TViewHolder;
import sviolet.turquoise.ui.drawable.SafeBitmapDrawable;
import sviolet.turquoise.util.bitmap.CachedBitmapUtils;

/**
 * ViewPager同时显示多个Item, 实现画廊效果
 *
 * Created by S.Violet on 2016/11/23.
 */
public class MultiItemViewPagerAdapter extends RecyclingPagerAdapter {

    private Activity context;
    private List<Integer> imageResIds;
    private CachedBitmapUtils cachedBitmapUtils;

    public MultiItemViewPagerAdapter(Activity context, List<Integer> imageResIds, CachedBitmapUtils cachedBitmapUtils) {
        this.context = context;
        this.imageResIds = imageResIds;
        this.cachedBitmapUtils = cachedBitmapUtils;
    }

    @Override
    public int getCount() {
        if (imageResIds == null) {
            return 0;
        }
        return imageResIds.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        TViewHolder holder = TViewHolder.create(context, convertView, container, R.layout.other_multiitem_viewpager_item);

        //设置图片
        Bitmap bitmap = cachedBitmapUtils.decodeFromResource(
                String.valueOf(imageResIds.get(position)),
                context.getResources(),
                imageResIds.get(position));
        ((ImageView)holder.get(R.id.other_multiitem_viewpager_viewpager_item_imageview)).setImageDrawable(new SafeBitmapDrawable(context.getResources(), bitmap));
        //view记录当前position
        holder.getConvertView().setTag(position);

        //注意:不要在这里设置OnClickListener, 会出问题的, 因为ViewPager不能很好的处理范围外的点击

        return holder.getConvertView();
    }

}
