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

package sviolet.turquoise.ui.dialog;

import android.app.Dialog;
import android.content.Context;

/**
 * 简易Dialog构造器
 *
 * Created by S.Violet on 2016/12/19.
 */
public interface SimpleDialogBuilder {

    /**
     * @param title 标题
     */
    void setTitle(String title);

    /**
     * @param content 内容
     */
    void setContent(String content);

    /**
     * @param msg 左按钮文字
     * @param callback 左按钮点击回调
     */
    void setLeftButton(String msg, Callback callback);

    /**
     * @param msg 中按钮文字
     * @param callback 中按钮点击回调
     */
    void setMiddleButton(String msg, Callback callback);

    /**
     * @param msg 右按钮文字
     * @param callback 右按钮点击回调
     */
    void setRightButton(String msg, Callback callback);

    /**
     * @param cancelable true:允许取消
     * @param callback 取消事件回调
     */
    void setCancelCallback(boolean cancelable, Callback callback);

    /**
     * 构建Dialog
     * @param context 不能是ApplicationContext
     */
    Dialog build(Context context);

    interface Callback{

        void callback();

    }

}
