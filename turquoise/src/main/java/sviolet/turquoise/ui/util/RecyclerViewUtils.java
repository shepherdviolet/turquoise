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

package sviolet.turquoise.ui.util;

import android.support.v7.widget.LinearLayoutManager;
import android.view.View;

/**
 * <p>RecyclerView工具</p>
 *
 * <p>Dependency:com.android.support:recyclerview-v7</p>
 *
 * Created by S.Violet on 2016/11/3
 */
public class RecyclerViewUtils {

    /**
     * 判断列表是否拉到最顶端
     * @param linearLayoutManager (LinearLayoutManager)recyclerView.getLayoutManager()
     */
    public static boolean reachTop(LinearLayoutManager linearLayoutManager){
        //无子元素视为到达顶端
        if (linearLayoutManager.getChildCount() <= 0){
            return true;
        }
        //第一个完全显示的子控件为第一个, 视为到达顶端
        if(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0){
            return true;
        }
        return false;
    }

    /**
     * 判断列表是否拉到最底端
     * @param linearLayoutManager (LinearLayoutManager)recyclerView.getLayoutManager()
     */
    public static boolean reachBottom(LinearLayoutManager linearLayoutManager){
        //无子元素视为到达底端
        if (linearLayoutManager.getChildCount() <= 0){
            return true;
        }
        //最后一个完全显示的Item为最后一个, 视为到达底端
        if(linearLayoutManager.findLastCompletelyVisibleItemPosition() == linearLayoutManager.getItemCount() - 1){
            return true;
        }
        return false;
    }

    /**
     * 列表从顶部开始向下滚动的进度
     * @param linearLayoutManager (LinearLayoutManager)recyclerView.getLayoutManager()
     * @return float 滚动进度(0-MAXVALUE), 并非滚动距离
     */
    public static float scrollProgressFromTop(LinearLayoutManager linearLayoutManager){
        if (linearLayoutManager.getChildCount() <= 0){
            return 0f;
        }
        View child = linearLayoutManager.getChildAt(0);
        if (child.getHeight() > 0){
            return linearLayoutManager.findFirstVisibleItemPosition() + ( - (float)child.getTop() / (float)child.getHeight());
        }else{
            return linearLayoutManager.findFirstVisibleItemPosition();
        }
    }

}
