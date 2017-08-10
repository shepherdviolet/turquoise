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

package sviolet.demoa.fingerprint.utils;

import android.support.annotation.NonNull;

import com.suke.widget.SwitchButton;

import java.lang.ref.WeakReference;

/**
 * [com.suke.widget.SwitchButton配套使用]
 *
 * 当开关触发一次事件后, 开关会被锁定不允许操作, 必须在回调方法onCheckedChangedEnhanced中调用releaseLock
 * 方法才能进行下一次开关操作. 用于防止开关反复操作造成逻辑问题.
 *
 * Created by S.Violet on 2017/8/10.
 */
public abstract class LockableSwitchButtonListener implements SwitchButton.OnCheckedChangeListener {

    private WeakReference<SwitchButton> switchButtonReference;
    private boolean lock = false;
    private boolean lockedState = false;

    public LockableSwitchButtonListener(@NonNull SwitchButton switchButton) {
        this.switchButtonReference = new WeakReference<>(switchButton);
    }

    public final void onCheckedChanged(SwitchButton view, boolean isChecked){
        //过滤重复开关
        if (lock){
            //将开关状态重置为"锁定时的状态"
            SwitchButton switchButton = switchButtonReference.get();
            if (switchButton != null) {
                switchButton.setChecked(lockedState);
            }
            return;
        }

        //记录锁定时的开关状态
        lockedState = isChecked;
        //锁定
        lock = true;
        //禁用开关
        SwitchButton switchButton = switchButtonReference.get();
        if (switchButton != null) {
            switchButton.setEnabled(false);
        }

        onCheckedChangedEnhanced(view, isChecked);

    }

    /**
     * 处理开关变化事件, 处理完成后必须调用releaseLock方法释放锁定, 方能进行下一次开关操作
     */
    public abstract void onCheckedChangedEnhanced(SwitchButton view, boolean isChecked);

    /**
     * 处理完成后必须调用releaseLock方法释放锁定, 方能进行下一次开关操作
     */
    public void releaseLock(){
        //释放锁
        lock = false;
        //启用开关
        SwitchButton switchButton = switchButtonReference.get();
        if (switchButton != null) {
            switchButton.setEnabled(true);
        }
    }

}
