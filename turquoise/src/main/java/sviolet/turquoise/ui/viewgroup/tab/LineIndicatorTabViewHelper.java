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

package sviolet.turquoise.ui.viewgroup.tab;

import android.content.Context;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>LineIndicatorTabView帮助类, 用于构建简单的TabView</p>
 *
 * <pre>{@code
 *      //实例化帮助类, 指定操作的lineIndicatorTabView, Item布局的layoutId, Item布局中TextView的id
 *      LineIndicatorTabViewHelper lineIndicatorTabViewHelper = new LineIndicatorTabViewHelper(
 *          getApplicationContext(),
 *          lineIndicatorTabViewForViewPager,
 *          R.layout.other_tab_view_tabitem,
 *          R.id.other_tab_view_tabitem_text);
 *
 *      //随时调用该方法, 更新TabView上的文字, 并可以指定初始位置
 *      lineIndicatorTabViewHelper.setData(tagStringList, 2);
 * }</pre>
 */
public class LineIndicatorTabViewHelper {

    private WeakReference<Context> contextReference;
    private WeakReference<LineIndicatorTabView> lineIndicatorTabViewReference;
    private int layoutId;
    private int textId;

    private List<View> viewCache = new ArrayList<>(4);

    public LineIndicatorTabViewHelper(Context context, LineIndicatorTabView lineIndicatorTabView, int layoutId, int textId) {
        if (context == null){
            throw new IllegalArgumentException("context is null");
        }
        if (lineIndicatorTabView == null){
            throw new IllegalArgumentException("lineIndicatorTabView is null");
        }
        if ((layoutId >>> 24) < 2) {
            throw new IllegalArgumentException("The layoutId must be an application-specific resource id.");
        }
        if ((textId >>> 24) < 2) {
            throw new IllegalArgumentException("The textId must be an application-specific resource id.");
        }

        this.contextReference = new WeakReference<>(context);
        this.lineIndicatorTabViewReference = new WeakReference<>(lineIndicatorTabView);
        this.layoutId = layoutId;
        this.textId = textId;
    }

    public void setData(List<String> textList, int initPage){
        if (Looper.getMainLooper() != Looper.myLooper()){
            throw new RuntimeException("LineIndicatorTabViewHelper.setData method must call in ui thread");
        }

        //context
        Context context = contextReference.get();
        //持有的LineIndicatorTabView
        LineIndicatorTabView lineIndicatorTabView = lineIndicatorTabViewReference.get();

        //弱引用持有, 若为空则销毁
        if (context == null || lineIndicatorTabView == null){
            return;
        }

        //清空TabItems
        lineIndicatorTabView.removeAllTabItems();

        if (textList == null){
            return;
        }

        //填充数据
        for (int i = 0 ; i < textList.size() ; i++){
            View view = fetchView(i, context);
            TextView textView = (TextView) view.getTag();
            textView.setText(textList.get(i));
            lineIndicatorTabView.addView(view);
        }

        //显示指定页
        lineIndicatorTabView.setToPage(initPage);

    }

    private View fetchView(int index, Context context){
        if (index < viewCache.size()){
            return viewCache.get(index);
        }

        try {
            View view = LayoutInflater.from(context).inflate(layoutId, null);
            TextView textView = (TextView) view.findViewById(textId);
            view.setTag(textView);
            viewCache.add(view);
            return view;
        } catch (Exception e) {
            throw new RuntimeException("[LineIndicatorTabViewHelper]error while inflate item view", e);
        }

    }

}
