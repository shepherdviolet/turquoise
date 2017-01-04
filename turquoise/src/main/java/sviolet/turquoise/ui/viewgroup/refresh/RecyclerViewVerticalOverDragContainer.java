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

package sviolet.turquoise.ui.viewgroup.refresh;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import sviolet.turquoise.ui.util.RecyclerViewUtils;

/**
 * <p>垂直方向越界拖动容器(RelativeLayout), 可用于实现下拉刷新上拉加载</p>
 *
 * <p>PARK: 即越界拖动超过设定值(overDragThreshold)后, 停止在设定位置, 用于实现下拉刷新/上拉加载,
 * PARK状态即下拉刷新中的状态.</p>
 *
 * <p>注意!!! 当发生PARK事件后, VerticalOverDragContainer会保持PARK状态, 不会再发生相同的PARK事件,
 * 必须调用resetTopPark/resetBottomPark方法, 重置状态, 才会再次发生PARK事件. 在实际使用中,
 * 接收到PARK事件时, 开始进行数据刷新, 当数据刷新完成后, 调用resetTopPark/resetBottomPark方法重置状态.
 * 当你使用{@link SimpleVerticalRefreshIndicatorGroup}配合实现下拉刷新时, 调用{@link SimpleVerticalRefreshIndicatorGroup}
 * 的{@link SimpleVerticalRefreshIndicatorGroup#reset(boolean)}可以起到相同的作用.</p>
 *
 * <p>
 *     支持的子控件:<br/>
 *     1.ScrollView<br/>
 *     2.ListView<br/>
 *     3.RecyclerView<br/>
 * </p>
 *
 * <p>
 *     将RecyclerViewVerticalOverDragContainer作为父控件, 将需要越界拖动的RecyclerView/ScrollView/ListView等作为子控件放置在其内部,
 *     原则上, RecyclerViewVerticalOverDragContainer只能容纳一个子控件, 如果必须要容纳多个控件, 请务必将RecyclerView/ScrollView/ListView等
 *     自身会滚动的控件放置在最后一个, RecyclerViewVerticalOverDragContainer会根据最后一个子控件是否到达顶部/底部来判断
 *     是否要发生越界滚动.
 * </p>
 *
 * <pre>{@code
 *      <sviolet.turquoise.ui.viewgroup.refresh.RecyclerViewVerticalOverDragContainer
 *          android:layout_width="match_parent"
 *          android:layout_height="match_parent"
 *          sviolet:VerticalOverDragContainer_overDragThreshold="70dp"
 *          sviolet:VerticalOverDragContainer_overDragResistance="0.4"
 *          sviolet:VerticalOverDragContainer_scrollDuration="700"
 *          sviolet:VerticalOverDragContainer_topParkEnabled="tue"
 *          sviolet:VerticalOverDragContainer_bottomParkEnabled="false"
 *          sviolet:VerticalOverDragContainer_disableIfHorizontalDrag="false">
 *
 *          ...
 *
 *          <android.support.v7.widget.RecyclerView
 *              android:layout_width="match_parent"
 *              android:layout_height="match_parent" />
 *
 *      </sviolet.turquoise.ui.viewgroup.refresh.RecyclerViewVerticalOverDragContainer>
 * }</pre>
 *
 * Created by S.Violet on 2016/11/3.
 */
public class RecyclerViewVerticalOverDragContainer extends VerticalOverDragContainer {

    public RecyclerViewVerticalOverDragContainer(Context context) {
        super(context);
    }

    public RecyclerViewVerticalOverDragContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerViewVerticalOverDragContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 复写checkReachState实现对RecyclerView的支持
     */
    @Override
    protected ReachState checkReachState(View child) {
        /**
         * RecyclerView的流程
         */
        if (child instanceof RecyclerView){
            boolean reachTop;
            boolean reachBottom;

            //判断RecyclerView是否到达顶部或底部
            RecyclerView.LayoutManager layoutManager = ((RecyclerView)child).getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                reachTop = RecyclerViewUtils.reachTop((LinearLayoutManager) layoutManager);
                reachBottom = RecyclerViewUtils.reachBottom((LinearLayoutManager) layoutManager);
            } else {
                throw new RuntimeException("[RecyclerViewVerticalOverDragContainer]child RecyclerView is not supported, view:" + child + ", layout manager:" + layoutManager);
            }

            if (reachTop && reachBottom) {
                return ReachState.REACH_BOTH;
            } else if (reachTop) {
                return ReachState.REACH_TOP;
            } else if (reachBottom) {
                return ReachState.REACH_BOTTOM;
            }
            return ReachState.HALFWAY;
        }
        /**
         * 其他控件的流程
         */
        return super.checkReachState(child);
    }
}
