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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.suke.widget.SwitchButton;

import sviolet.turquoise.ui.util.motion.MultiClickFilter;
import sviolet.turquoise.util.droid.MotionEventUtils;

/**
 * 改版SwitchButton
 * 过于频繁的UP事件会被模拟成CANCEL事件, 过滤快速点击
 *
 * Created by S.Violet on 2017/8/10.
 */
public class EnhancedSwitchButton extends SwitchButton {

    private MultiClickFilter multiClickFilter = new MultiClickFilter();

    public EnhancedSwitchButton(Context context) {
        super(context);
    }

    public EnhancedSwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EnhancedSwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public EnhancedSwitchButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        //过于频繁的UP事件会被模拟成CANCEL事件, 过滤快速点击
        if (event.getActionMasked() == MotionEvent.ACTION_UP){
            if (!multiClickFilter.tryHandle()){
                MotionEventUtils.emulateCancelEvent(event, this);
                return true;
            }
        }
        return super.dispatchTouchEvent(event);
    }

}
