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

package sviolet.demoa.slide;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhanced.annotation.inject.ResourceId;
import sviolet.turquoise.enhanced.TActivity;
import sviolet.turquoise.utils.sys.MeasureUtils;
import sviolet.turquoise.view.listener.OnSlideStopListener;
import sviolet.turquoise.view.slide.logic.LinearGestureDriver;
import sviolet.turquoise.view.slide.view.RelativeLayoutDrawer;

/**
 * 可侧滑关闭Activity的demo(子Activity)
 * Created by S.Violet on 2015/6/30.
 */
@ResourceId(R.layout.slide_activity_sub)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class SubActivitySlideActivity extends TActivity {

    @ResourceId(R.id.slide_activity_sub_drawer)
    private RelativeLayoutDrawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //设置抽屉控件
        drawer.setSlideScrollDirection(RelativeLayoutDrawer.DIRECTION_RIGHT)
                .setSlideScrollDuration(500)
                .setSlideDrawerWidth(RelativeLayoutDrawer.DRAWER_WIDTH_MATCH_PARENT)
                .setSlideInitStage(RelativeLayoutDrawer.STAGE_PULL_OUT)
                .applySlideSetting();
        //设置触摸有效区域
        drawer.getGestureDriver().setTouchArea(LinearGestureDriver.TOUCH_AREA_MODE_VALID,
                0, MeasureUtils.dp2px(getApplicationContext(), 30), Integer.MIN_VALUE, Integer.MAX_VALUE);
        //滑动停止监听器
        drawer.setOnSlideStopListener(new OnSlideStopListener() {
            @Override
            public void onStop() {
                //抽屉关闭时结束Activity
                if (drawer.getCurrentStage() == drawer.getPushInStage()){
                        finish();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            //拦截返回键改为关闭抽屉
            drawer.pushIn();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(getApplicationContext(), "finish", Toast.LENGTH_SHORT).show();
    }

}
