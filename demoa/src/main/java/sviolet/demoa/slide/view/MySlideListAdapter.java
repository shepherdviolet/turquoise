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

package sviolet.demoa.slide.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sviolet.demoa.R;
import sviolet.turquoise.ui.util.TViewHolder;
import sviolet.turquoise.uix.slideengine.view.LinearLayoutDrawer;
import sviolet.turquoise.uix.slideengine.view.SlideListAdapter;

/**
 * 滑动列表适配器
 * <p/>
 * Created by S.Violet on 2015/6/23.
 */
public class MySlideListAdapter extends BaseAdapter implements SlideListAdapter {

    private Context context;
    private int quantity;//列表项数量
    private String title;//标题
    private String type;//类型
    private String info;//信息
    private int titleColor;//标题字体颜色

    private List<LinearLayoutDrawer> drawerList = new ArrayList<LinearLayoutDrawer>();

    /**
     * @param quantity 列表项数量
     * @param title    标题
     * @param type     类型
     * @param info     说明
     */
    public MySlideListAdapter(Context context, int quantity, String title, String type, String info) {
        this(context, quantity, title, type, info, 0xFF303030);
    }

    /**
     * @param quantity   列表项数量
     * @param title      标题
     * @param type       类型
     * @param info       说明
     * @param titleColor 标题颜色
     */
    public MySlideListAdapter(Context context, int quantity, String title, String type, String info, int titleColor) {
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
        TViewHolder holder = TViewHolder.create(context, convertView, parent, R.layout.slide_list_item);

        final LinearLayoutDrawer drawerView = (LinearLayoutDrawer) holder.get(R.id.slide_list_drawer);
        TextView titleView = (TextView) holder.get(R.id.common_list_item_title);
        TextView typeView = (TextView) holder.get(R.id.common_list_item_type);
        TextView infoView = (TextView) holder.get(R.id.common_list_item_info);
        Button backgroundButton = (Button) holder.get(R.id.slide_list_background_button);

        /*
            初始化, 当createTimes() == 1 时, convertView为新实例, 在此时可以进行控件初始化操作.
         */

        if (holder.createTimes() == 1){
            //初始化抽屉
            drawerList.add(drawerView);
            drawerView.setSlideScrollDirection(LinearLayoutDrawer.DIRECTION_LEFT)
                    .setSlideDrawerWidth(200)
                    .setSlideScrollDuration(700)
                    .setSlideInitStage(LinearLayoutDrawer.STAGE_PULL_OUT)
                    .applySlideSetting();
            //设置监听器
            titleView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "title click", Toast.LENGTH_SHORT).show();
                }
            });
            backgroundButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context, "button click", Toast.LENGTH_SHORT).show();
                    drawerView.pullOut();
                }
            });
        }else{
            //重置抽屉
            drawerView.pullOutImmediately();//拉出抽屉
        }

        /*
         * 填充数据
         */

        String tail = Integer.toString(position);
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

        /*
         * 返回
         */

        return holder.getConvertView();
    }

    /**********************************************
     * private
     */

    @Override
    public boolean hasSliddenItem() {
        for (LinearLayoutDrawer linearLayoutDrawer : drawerList) {
            if (linearLayoutDrawer.getCurrentStage() != linearLayoutDrawer.getPullOutStage())
                return true;
        }
        return false;
    }

    @Override
    public void resetSliddenItem() {
        for (LinearLayoutDrawer linearLayoutDrawer : drawerList) {
            linearLayoutDrawer.pullOut();
        }
    }
}
