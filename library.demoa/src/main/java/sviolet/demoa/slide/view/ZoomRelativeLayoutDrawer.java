/*
 * Copyright (C) 2015 S.Violet
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
 */

package sviolet.demoa.slide.view;

import sviolet.turquoise.utils.sys.DeviceUtils;
import sviolet.turquoise.view.slide.view.LayoutDrawerProvider;
import sviolet.turquoise.view.slide.view.RelativeLayoutDrawer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * 自定义缩放的RelativeLayoutDrawer
 *
 * @author S.Violet
 */
public class ZoomRelativeLayoutDrawer extends RelativeLayoutDrawer {

    public ZoomRelativeLayoutDrawer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ZoomRelativeLayoutDrawer(Context context) {
        super(context);
    }

    @Override
    public void computeScroll() {
        if (getSlideEngine() != null) {
            //滑动
            switch (getScrollDirection()) {
                case LayoutDrawerProvider.DIRECTION_TOP:
                    scrollTo(0, getSlideEngine().getPosition());
                    break;
                case LayoutDrawerProvider.DIRECTION_BOTTOM:
                    scrollTo(0, getSlideEngine().getPosition() - getSlideEngine().getRange());
                    break;
                case LayoutDrawerProvider.DIRECTION_LEFT:
                    scrollTo(getSlideEngine().getPosition(), 0);
                    break;
                case LayoutDrawerProvider.DIRECTION_RIGHT:
                    scrollTo(getSlideEngine().getPosition() - getSlideEngine().getRange(), 0);
                    break;
            }
            //Sdk11以上可用setScaleX/Y方法缩放
            if (DeviceUtils.getVersionSDK() >= 11)
                scale();
            //变色
            changeColor();

            if (!getSlideEngine().isStop()) {
                postInvalidate();
            }
        }
    }

    /**
     * 变色
     */
    private void changeColor() {
        int color = ((int) (getSlideEngine().getCurrentStage() * 192)) << 24;
        setBackgroundColor(color);
    }

    /**
     * 缩放
     */
    @SuppressLint("NewApi")
    private void scale() {
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setScaleX(getSlideEngine().getCurrentStage() / 5 + 0.8f);
            view.setScaleY(getSlideEngine().getCurrentStage() / 5 + 0.8f);
        }
    }

}
