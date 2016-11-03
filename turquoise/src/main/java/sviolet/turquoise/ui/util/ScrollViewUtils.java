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

import android.widget.ScrollView;

/**
 * <p>ScrollView工具</p>
 *
 * Created by S.Violet on 2016/11/3.
 */
public class ScrollViewUtils {

    /**
     * 判断ScrollView是否拉到最顶端
     * @param scrollView 控件
     */
    public static boolean reachTop(ScrollView scrollView){
        //无子元素视为到达顶端
        if (scrollView.getChildCount() <= 0){
            return true;
        }
        //Y方向滚动距离为0视为到顶部
        if(scrollView.getScrollY() <= 0){
            return true;
        }
        return false;
    }

    /**
     * 判断ScrollView是否拉到最底端
     * @param scrollView 控件
     */
    public static boolean reachBottom(ScrollView scrollView){
        //无子元素视为到达底端
        if (scrollView.getChildCount() <= 0){
            return true;
        }
        //Y方向滚动距离等于子控件高度减去ScrollView高度时,视为到达底部
        if(scrollView.getChildAt(0).getHeight() - scrollView.getHeight() <= scrollView.getScrollY()){
            return true;
        }
        return false;
    }

    /**
     * ScrollView从顶部开始向下滚动的进度
     * @param scrollView 控件
     * @return float 滚动进度(0-1), 并非滚动距离
     */
    public static float scrollProgressFromTop(ScrollView scrollView){
        if (scrollView.getChildCount() <= 0){
            return 0f;
        }
        return (float)scrollView.getScrollY() / (float)(scrollView.getChildAt(0).getHeight() - scrollView.getHeight());
    }

}
