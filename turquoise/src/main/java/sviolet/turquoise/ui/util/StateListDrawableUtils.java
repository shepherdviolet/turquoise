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

package sviolet.turquoise.ui.util;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

/**
 * StateListDrawable(Selector)工具
 *
 * Created by S.Violet on 2016/12/19.
 */
public class StateListDrawableUtils {

    /**
     * 生成按下/点击/选中效果的Selector
     * @param normal 正常状态
     * @param pressed 按下/点击/选中状态
     */
    public static StateListDrawable createPressSelector(Drawable normal, Drawable pressed) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{-android.R.attr.state_focused, -android.R.attr.state_selected, -android.R.attr.state_pressed}, normal);
        drawable.addState(new int[]{-android.R.attr.state_focused, android.R.attr.state_selected, -android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[]{android.R.attr.state_focused, -android.R.attr.state_selected, -android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[]{android.R.attr.state_focused, android.R.attr.state_selected, -android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[]{android.R.attr.state_selected, android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        return drawable;
    }

}
