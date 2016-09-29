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

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;

/**
 * <p>View工具</p>
 *
 * Created by S.Violet on 2016/9/28.
 */

public class ViewCommonUtils {

    /**
     * <p>利用view.getViewTreeObserver().addOnPreDrawListener(...)方法捕获View第一次绘制前的事件,
     * 常用于View的初始化操作. 因为View在实例化后, layout之前, 是获取不到控件长宽的, 在InitListener.onInit()
     * 被回调时, View的长宽已经计算完成, 因此可以利用这个方法获取控件长宽, 并完成控件初始化.</p>
     *
     * @param view 控件
     * @param initListener 初始化回调
     */
    public static void setInitListener(@NonNull View view, final InitListener initListener) {

        final WeakReference<View> viewReference = new WeakReference<>(view);

        //View获取自身宽高等参数方法
        //绘制监听器, 也可以使用addOnGlobalLayoutListener监听layout事件
        view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                //移除监听器, 以免重复调用
                View view = viewReference.get();
                if (view != null) {
                    view.getViewTreeObserver().removeOnPreDrawListener(this);
                }
                if (initListener != null) {
                    initListener.onInit();
                }
                return true;
            }
        });

    }

    public interface InitListener {

        void onInit();

    }

}