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

package sviolet.turquoise.uix.viewgesturectrl;

/**
 * <p>ViewGestureController点击事件监听器</p>
 *
 * Created by S.Violet on 2016/9/23.
 */
public interface ViewGestureClickListener extends ViewGestureOutput {

    /**
     * 点击
     * @param x x
     * @param y y
     */
    void onClick(float x, float y);

    /**
     * 长按
     * @param x x
     * @param y y
     */
    void onLongClick(float x, float y);

}
