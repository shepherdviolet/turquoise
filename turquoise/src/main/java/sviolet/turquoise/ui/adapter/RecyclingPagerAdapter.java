/*
 * Copyright (C) 2015-2017 S.Violet
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

package sviolet.turquoise.ui.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.HashSet;
import java.util.Set;

import sviolet.turquoise.entity.statics.PublicConstants;

/**
 * <p>A {@link PagerAdapter} which behaves like an {@link android.widget.Adapter} with view types and
 * view recycling.</p>
 *
 * <p>一个带View回收复用的PagerAdapter, 将PagerAdapter的实现方式改的像ListView.Adapter那样.</p>
 *
 * @author Jake Wharton
 */
public abstract class RecyclingPagerAdapter extends PagerAdapter {

    protected static final int IGNORE_ITEM_VIEW_TYPE = AdapterView.ITEM_VIEW_TYPE_IGNORE;

    private final RecycleBin recycleBin;

    private Set<Integer> activePositionSet = new HashSet<>();//当前有效的Item集合
    private Set<Integer> refreshPositionSet = new HashSet<>();//需要刷新的Item集合

    public RecyclingPagerAdapter() {
        this(new RecycleBin());
    }

    RecyclingPagerAdapter(RecycleBin recycleBin) {
        this.recycleBin = recycleBin;
        recycleBin.setViewTypeCount(getViewTypeCount());
    }

    @Override
    public void notifyDataSetChanged() {
        recycleBin.scrapActiveViews();
        super.notifyDataSetChanged();
    }

    /**
     * <p>刷新指定页面, PagerAdapter通常用notifyDataSetChanged是不会刷新的, 需要用该方法实现</p>
     *
     * <p>NOTE:如果ViewPager设置了PageTransformer, 刷新时View是从RecycleBin中获取的, View会保持
     * 缩放等状态, 导致刷新后动画状态错乱, 而PageTransformer只有在滚动时才会执行计算.
     * 强制让ViewPager滚动即可解决该问题, 方法如下:</p>
     *
     * <pre>{@code
     *      //刷新显示
     *      viewPagerAdapter.refreshAll();
     *      //先设置当前Item为第一个或最后一个
     *      viewPager.setCurrentItem(position > 0 ? 0 : Integer.MAX_VALUE, false);
     *      //滚动到指定Item
     *      viewPager.setCurrentItem(position, true);
     * }</pre>
     *
     * @param page 指定刷新的页码
     */
    public void refresh(int page){
        refreshPositionSet.add(page);
        notifyDataSetChanged();
    }

    /**
     * <p>刷新全部页面, PagerAdapter通常用notifyDataSetChanged是不会刷新的, 需要用该方法实现</p>
     *
     * <p>NOTE:如果ViewPager设置了PageTransformer, 刷新时View是从RecycleBin中获取的, View会保持
     * 缩放等状态, 导致刷新后动画状态错乱, 而PageTransformer只有在滚动时才会执行计算.
     * 强制让ViewPager滚动即可解决该问题, 方法如下:</p>
     *
     * <pre>{@code
     *      //刷新显示
     *      viewPagerAdapter.refreshAll();
     *      //先设置当前Item为第一个或最后一个
     *      viewPager.setCurrentItem(position > 0 ? 0 : Integer.MAX_VALUE, false);
     *      //滚动到指定Item
     *      viewPager.setCurrentItem(position, true);
     * }</pre>
     *
     */
    public void refreshAll(){
        refreshPositionSet.addAll(activePositionSet);
        notifyDataSetChanged();
    }

    /**
     * 实现了刷新指定Item
     */
    @Override
    public int getItemPosition(Object object) {
        if (object instanceof View) {
            Object tag = ((View) object).getTag(PublicConstants.ViewTag.RecyclingPagerAdapterPosition);
            if (tag instanceof Integer) {
                //在刷新集合中的Item, 需要重建
                if (refreshPositionSet.remove(tag)){
                    return POSITION_NONE;
                }
            }
        }
        //其他Item不需要重建
        return super.getItemPosition(object);
    }

    @Override
    public final Object instantiateItem(ViewGroup container, int position) {
        //view type
        int viewType = getItemViewType(position);
        View view = null;
        //从回收站获取view
        if (viewType != IGNORE_ITEM_VIEW_TYPE) {
            view = recycleBin.getScrapView(position, viewType);
        }
        view = getView(position, view, container);
        container.addView(view);
        view.setTag(PublicConstants.ViewTag.RecyclingPagerAdapterPosition, position);//记录position
        activePositionSet.add(position);//塞入有效Item集合
        return view;
    }

    @Override
    public final void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
        //view type
        int viewType = getItemViewType(position);
        //将view放入回收站
        if (viewType != IGNORE_ITEM_VIEW_TYPE) {
            recycleBin.addScrapView(view, position, viewType);
        }
        activePositionSet.remove(position);//从有效Item集合中移除
    }

    @Override
    public final boolean isViewFromObject(View view, Object object) {
        //默认实现
        return view == object;
    }

    /**
     * <p>
     * Returns the number of types of Views that will be created by
     * {@link #getView}. Each type represents a set of views that can be
     * converted in {@link #getView}. If the adapter always returns the same
     * type of View for all items, this method should return 1.
     * </p>
     * <p>
     * This method will only be called when when the adapter is set on the
     * the {@link AdapterView}.
     * </p>
     *
     * @return The number of types of Views that will be created by this adapter
     */
    public int getViewTypeCount() {
        //view type的数量
        return 1;
    }

    /**
     * Get the type of View that will be created by {@link #getView} for the specified item.
     *
     * @param position The position of the item within the adapter's data set whose view type we
     *                 want.
     * @return An integer representing the type of View. Two views should share the same type if one
     * can be converted to the other in {@link #getView}. Note: Integers must be in the
     * range 0 to {@link #getViewTypeCount} - 1. {@link #IGNORE_ITEM_VIEW_TYPE} can
     * also be returned.
     * @see #IGNORE_ITEM_VIEW_TYPE
     */
    @SuppressWarnings("UnusedParameters") // Argument potentially used by subclasses.
    public int getItemViewType(int position) {
        //根据position返回view type
        return 0;
    }

    /**
     * Get a View that displays the data at the specified position in the data set. You can either
     * create a View manually or inflate it from an XML layout file. When the View is inflated, the
     * parent View (GridView, ListView...) will apply default layout parameters unless you use
     * {@link android.view.LayoutInflater#inflate(int, ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the item whose view
     *                    we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     *                    Heterogeneous lists can specify their number of view types, so that this View is
     *                    always of the right type (see {@link #getViewTypeCount()} and
     *                    {@link #getItemViewType(int)}).
     * @param container   The parent that this view will eventually be attached to
     * @return A View corresponding to the data at the specified position.
     */
    public abstract View getView(int position, View convertView, ViewGroup container);

}