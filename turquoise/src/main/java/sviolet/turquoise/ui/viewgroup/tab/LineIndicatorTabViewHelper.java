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

    /**
     * @param context context
     * @param lineIndicatorTabView tabView实例
     * @param layoutId Item布局的layoutId
     * @param textId 布局中TextView的Id
     * @param textColorNormal 未选中时字体颜色
     * @param textColorSelected 选中时字体颜色
     */
    public LineIndicatorTabViewHelper(Context context, LineIndicatorTabView lineIndicatorTabView, int layoutId, int textId, final int textColorNormal, final int textColorSelected){
        this(context, lineIndicatorTabView, layoutId, textId);

        //实现文字颜色变化效果
        lineIndicatorTabView.addOnPageChangedListener(new LineIndicatorTabView.OnPageChangedListener() {

            private WeakReference<TextView> lastTextView;//持有上一个变色的Item

            @Override
            public void onPageChanged(int page, View child, boolean byClick) {
                if (child == null){
                    return;
                }
                //将前一个Item置为未选中颜色
                if (lastTextView != null) {
                    TextView lastTextViewInstance = lastTextView.get();
                    if (lastTextViewInstance != null) {
                        lastTextViewInstance.setTextColor(textColorNormal);
                    }
                }
                Object tag = child.getTag();
                if (tag instanceof TextView) {
                    //设置当前Item的颜色
                    ((TextView) tag).setTextColor(textColorSelected);
                    //记录当前Item
                    this.lastTextView = new WeakReference<>((TextView) tag);
                }
            }
        });
    }

    /**
     * @param context context
     * @param lineIndicatorTabView tabView实例
     * @param layoutId Item布局的layoutId
     * @param textId 布局中TextView的Id
     */
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

    /**
     * 设置数据并刷新
     * @param textList 数据
     * @param initPage 起始页
     */
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
