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
 *
 * Project GitHub: https://github.com/shepherdviolet/turquoise
 * Email: shepherdviolet@163.com
 */

package sviolet.demoa.slide;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EmulateListAdapter;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.ui.view.shadow.LinearShadowView;
import sviolet.turquoise.uix.slideengine.listener.OnSlideStopListener;
import sviolet.turquoise.uix.slideengine.view.RelativeLayoutDrawer;

@DemoDescription(
        title = "Drawer",
        type = "View",
        info = "this is a Drawer demo made by turquoise.view.slide"
)

/**
 * 抽屉控件Demo
 */

@ResourceId(R.layout.slide_drawer)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class DrawerSlideActivity extends TActivity {

    @ResourceId(R.id.slide_drawer_listview)
    private ListView backgroundListView;
    @ResourceId(R.id.slide_drawer_leftdrawer)
    private RelativeLayoutDrawer leftdrawer;
    @ResourceId(R.id.slide_drawer_leftdrawer_listview)
    private ListView leftdrawerListview;
    @ResourceId(R.id.slide_drawer_leftdrawer_rightshadow)
    private LinearShadowView leftdrawerRightshadow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        backgroundListView.setAdapter(new EmulateListAdapter(this, 30,
                "DrawerBackgroundList", "10 hours ago", "this is a Drawer demo made by turquoise.view.slide"));
        leftdrawerListview.setAdapter(new EmulateListAdapter(this, 5, "Title", null, null));

        //抽屉滑动配置
        leftdrawer
                .setSlideScrollDirection(RelativeLayoutDrawer.DIRECTION_LEFT)//设置抽屉方向
                .setSlideScrollDuration(700)//设置惯性滑动时间
                .setSlideDrawerWidth(280)//设置抽屉宽度
                .setSlideHandleWidth(50)//设置把手宽度(dp)
                .setSlideInitStage(RelativeLayoutDrawer.STAGE_PUSH_IN)//设置默认状态:拉出
                .setSlideOverScrollEnabled(true)//设置允许越界拖动
                .setHandleFeedback(true)//把手触摸反馈
                .setHandleFeedbackRange(25)//把手触摸反馈幅度
                .setOnSlideStopListener(new OnSlideStopListener() {
                    @Override
                    public void onStop() {
                        //当抽屉收起时隐藏阴影
                        if (leftdrawer.getCurrentStage() == leftdrawer.getPushInStage()){
                            leftdrawerRightshadow.setVisibility(View.INVISIBLE);
                        }
                    }
                })
                .setOnGestureHoldListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //拖动时显示阴影
                        leftdrawerRightshadow.setVisibility(View.VISIBLE);
                    }
                })
                .setOnHandleTouchListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //把手触摸时显示阴影
                        leftdrawerRightshadow.setVisibility(View.VISIBLE);
                    }
                })
//			.setOnHandleClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
                    //点击把手拉开抽屉
//					leftdrawer.pullOut(true);
//				}
//			})
//			.setOnHandleLongPressListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//                  //长按
//				}
//			})
                .applySlideSetting();//应用设置

    }

}
