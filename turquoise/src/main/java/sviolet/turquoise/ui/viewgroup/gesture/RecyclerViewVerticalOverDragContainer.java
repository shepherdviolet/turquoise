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

package sviolet.turquoise.ui.viewgroup.gesture;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import sviolet.turquoise.ui.util.RecyclerViewUtils;

/**
 * <p>支持RecyclerView的</p>
 *
 * Created by S.Violet on 2016/11/7.
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

    @Override
    protected ReachState checkReachState(View child) {
        if (child instanceof RecyclerView){
            boolean reachTop;
            boolean reachBottom;

            RecyclerView.LayoutManager layoutManager = ((RecyclerView)child).getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                reachTop = RecyclerViewUtils.reachTop((LinearLayoutManager) layoutManager);
                reachBottom = RecyclerViewUtils.reachBottom((LinearLayoutManager) layoutManager);
            } else {
                throw new RuntimeException("[RecyclerVerticalOverDragContainer]child RecyclerView is not supported, view:" + child + ", layout manager:" + layoutManager);
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
        return super.checkReachState(child);
    }
}
