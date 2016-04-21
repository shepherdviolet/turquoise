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

package sviolet.demoaimageloader.demos.extra;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sviolet.demoaimageloader.R;
import sviolet.turquoise.ui.util.ViewHolder;
import sviolet.turquoise.x.imageloader.TILoader;
import sviolet.turquoise.x.imageloader.TILoaderUtils;

/**
 * ListView适配器
 * Created by S.Violet on 2015/7/7.
 */
public class ListViewAdapter extends BaseAdapter {

    private Activity context;
    private List<AsyncImageItem> itemList;

    /**
     * @param context context
     * @param itemList 数据
     */
    public ListViewAdapter(Activity context, List<AsyncImageItem> itemList){
        this.context = context;
        this.itemList = itemList;
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

        ViewHolder holder = ViewHolder.create(context, convertView, parent, R.layout.list_view_main_item);

        AsyncImageItem item = itemList.get(position);
        ((TextView) holder.get(R.id.list_view_main_item_title)).setText(item.getTitle());
        ((TextView) holder.get(R.id.list_view_main_item_content)).setText(item.getContent());

        ImageView[] images = new ImageView[5];
        images[0] = (ImageView) holder.get(R.id.list_view_main_item_imageview0);
        images[1] = (ImageView) holder.get(R.id.list_view_main_item_imageview1);
        images[2] = (ImageView) holder.get(R.id.list_view_main_item_imageview2);
        images[3] = (ImageView) holder.get(R.id.list_view_main_item_imageview3);
        images[4] = (ImageView) holder.get(R.id.list_view_main_item_imageview4);

        if (holder.createTimes() == 1){
            for (int i = 0 ; i < 5 ; i++) {
                final ImageView imageView = images[i];
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TILoaderUtils.reloadView(imageView);
                    }
                });
            }
        }

        for (int i = 0 ; i < 5 ; i++) {
            TILoader.node(context).load(item.getUrl(i), images[i]);
        }

        return holder.getConvertView();
    }

}
