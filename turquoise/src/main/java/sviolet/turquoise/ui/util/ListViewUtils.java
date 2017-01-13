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

import android.view.View;
import android.widget.AbsListView;

/**
 * <p>ListView工具</p>
 *
 * Created by S.Violet on 2016/3/9.
 */
public class ListViewUtils {

    /**
     * 判断列表是否拉到最顶端
     * @param listView 列表控件
     */
    public static boolean reachTop(AbsListView listView){
        //无子元素视为到达顶端
        if (listView.getChildCount() <= 0){
            return true;
        }
        //显示的第一个Item为第一个, 且该Item到达顶端的距离为0
        if(listView.getFirstVisiblePosition() == 0 && listView.getChildAt(0).getTop() >= 0){
            return true;
        }
        return false;
    }

    /**
     * 判断列表是否拉到最底端
     * @param listView 列表控件
     */
    public static boolean reachBottom(AbsListView listView){
        //无子元素视为到达底端
        if (listView.getChildCount() <= 0){
            return true;
        }
        //显示的最后一个Item为最后一个, 且该Item到达底部的距离为0
        if(listView.getLastVisiblePosition() == (listView.getCount() - 1) &&
                listView.getChildAt(listView.getChildCount() - 1).getBottom() <= listView.getHeight()){
            return true;
        }
        return false;
    }

    /**
     * 列表从顶部开始向下滚动的进度
     * @param listView 列表控件
     * @return float 滚动进度(0-MAXVALUE), 并非滚动距离
     */
    public static float scrollProgressFromTop(AbsListView listView){
        if (listView.getChildCount() <= 0){
            return 0f;
        }
        View child = listView.getChildAt(0);
        if (child.getHeight() > 0){
            return listView.getFirstVisiblePosition() + ( - (float)child.getTop() / (float)child.getHeight());
        }else{
            return listView.getFirstVisiblePosition();
        }
    }

}
