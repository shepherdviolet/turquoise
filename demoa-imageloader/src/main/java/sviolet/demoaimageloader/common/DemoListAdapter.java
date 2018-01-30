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

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import sviolet.demoaimageloader.R;
import sviolet.turquoise.ui.adapter.TViewHolder;

/**
 * Demo列表适配器
 *
 * Created by S.Violet on 2015/6/2.
 */
public class DemoListAdapter extends BaseAdapter {

    private Context context;
    private int resId;
    private Class<? extends Activity>[] activityList;

    /**
     *
     * @param context context
     * @param activityList 要显示的activity
     */
    public DemoListAdapter(Context context, int resId, Class<? extends Activity>[] activityList) {
        this.context = context;
        this.resId = resId;
        this.activityList = activityList;
    }

    @Override
    public int getCount() {
        if (activityList != null) {
            return activityList.length;
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (activityList != null && position < activityList.length && position >= 0) {
            return activityList[position];
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TViewHolder holder = TViewHolder.create(context, convertView, parent, resId);
        inflateView(position, holder);
        return holder.getConvertView();
    }

    /****************************************************
     * private
     */

    /**
     * 渲染View
     * @param position 位置
     * @param holder holder
     */
    private void inflateView(int position, TViewHolder holder) {
        Class<? extends Activity> activity = (Class) getItem(position);
        if (activity == null) {
            return;
        }
        if (activity.isAnnotationPresent(DemoDescription.class)) {
            DemoDescription description = activity.getAnnotation(DemoDescription.class);
            setViewParams(holder, description.title(), description.type(), description.info());
        } else {
            setViewParams(holder, null, null, null);
        }
    }

    /**
     * 设置View的显示值
     */
    private void setViewParams(TViewHolder holder, String title, String type, String info) {
        if (holder == null) {
            return;
        }

        if (title != null) {
            holder.<TextView>get(R.id.guide_main_item_title).setText(title);
        } else {
            holder.<TextView>get(R.id.guide_main_item_title).setText("未设置@DemoDescription");
        }

        holder.<TextView>get(R.id.guide_main_item_type).setText(type);
        holder.<TextView>get(R.id.guide_main_item_info).setText(info);
    }

}
