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

package sviolet.demoaimageloader.common;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import sviolet.demoaimageloader.R;
import sviolet.turquoise.ui.util.ViewHolder;

/**
 * 模拟List适配器
 * <p/>
 * Created by S.Violet on 2015/6/3.
 */
public class EmulateListAdapter extends BaseAdapter {

    private Context context;
    private int quantity;//列表项数量
    private String title;//标题
    private String type;//类型
    private String info;//信息
    private int titleColor;//标题字体颜色

    /**
     * @param quantity 列表项数量
     * @param title 标题
     * @param type 类型
     * @param info 说明
     */
    public EmulateListAdapter(Context context, int quantity, String title, String type, String info){
        this(context, quantity, title, type, info, 0xFF303030);
    }

    /**
     * @param quantity 列表项数量
     * @param title 标题
     * @param type 类型
     * @param info 说明
     * @param titleColor 标题颜色
     */
    public EmulateListAdapter(Context context, int quantity, String title, String type, String info, int titleColor) {
        this.context = context;
        this.quantity = quantity;
        this.title = title;
        this.type = type;
        this.info = info;
        this.titleColor = titleColor;
    }

    @Override
    public int getCount() {
        return quantity;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = ViewHolder.create(context, convertView, parent, R.layout.common_list_item);
        inflateView(position, holder);
        return holder.getConvertView();
    }

    /**********************************************
     * private
     */

    /**
     * 渲染View
     *
     * @param position 位置
     * @param holder   holder
     */
    private void inflateView(int position, ViewHolder holder) {
        String tail = Integer.toString(position);

        TextView titleView = (TextView) holder.get(R.id.common_list_item_title);
        TextView typeView = (TextView) holder.get(R.id.common_list_item_type);
        TextView infoView = (TextView) holder.get(R.id.common_list_item_info);

        if (title != null)
            titleView.setText(title + tail);
        else
            titleView.setText("");

        titleView.setTextColor(titleColor);

        if (type != null)
            typeView.setText(type + tail);
        else
            typeView.setText("");

        if (info != null)
            infoView.setText(info + tail);
        else
            infoView.setText("");

    }

}
