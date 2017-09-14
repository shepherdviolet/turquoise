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

package sviolet.demoa.info;

import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.demoa.common.EnhancedSwitchButton;
import sviolet.demoa.common.LockableSwitchButtonListener;
import sviolet.demoa.info.utils.LayoutInspectorAccessibilityModule;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.util.droid.AccessibilityUtils;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.DrawOverlaysUtils;
import sviolet.turquoise.utilx.accessibility.AccessibilityContainerService;

/**
 * 布局分析器设置界面(AccessibilityService+叠加层实现)
 *
 * Created by S.Violet on 2017/9/11.
 */
@DemoDescription(
        title = "Layout Inspector",
        type = "Info",
        info = "Layout inspector implements by AccessibilityService and floating window"
)

@ResourceId(R.layout.info_layout_inspector_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class LayoutInspectorInfoActivity extends TActivity {

    public static final int REQUIRED_API = Build.VERSION_CODES.JELLY_BEAN_MR2;

    @ResourceId(R.id.info_layout_inspector_main_switch_button)
    private EnhancedSwitchButton switchButton;
    @ResourceId(R.id.info_layout_inspector_main_overlays_button)
    private View overlaysButton;
    @ResourceId(R.id.info_layout_inspector_main_accessibility_button)
    private View accessibilityButton;
    @ResourceId(R.id.info_layout_inspector_main_textview)
    private TextView noticeView;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {

        //判断版本
        if (DeviceUtils.getVersionSDK() < REQUIRED_API){
            switchButton.setEnabled(false);
            return;
        }

        // LockableSwitchButtonListener:当开关触发一次事件后, 开关会被锁定不允许操作, 必须在回调方法onCheckedChangedEnhanced中调用releaseLock
        // 方法才能进行下一次开关操作. 用于防止开关反复操作造成逻辑问题.
        switchButton.setOnCheckedChangeListener(new LockableSwitchButtonListener(switchButton) {
            @Override
            public void onCheckedChangedEnhanced(SwitchButton view, boolean isChecked) {
                //解锁
                releaseLock();
                //刷新开关
                myHandler.sendEmptyMessage(MyHandler.REFRESH);

                if (isChecked) {

                    //引导用户开启叠加层权限
                    if (!DrawOverlaysUtils.canDrawOverlays(LayoutInspectorInfoActivity.this)) {
                        toEnableDrawOverlays();
                        return;
                    }

                    //引导用户开启辅助功能权限
                    if (!AccessibilityUtils.isServiceEnabled(LayoutInspectorInfoActivity.this, getPackageName())) {
                        toEnableAccessibility();
                        return;
                    }

                    //启用检测器
                    LayoutInspectorAccessibilityModule accessibilityModule = AccessibilityContainerService.getModule(LayoutInspectorAccessibilityModule.class);
                    if (accessibilityModule != null){
                        accessibilityModule.setEnabled(true);
                    }

                } else {

                    //禁用检测器
                    LayoutInspectorAccessibilityModule accessibilityModule = AccessibilityContainerService.getModule(LayoutInspectorAccessibilityModule.class);
                    if (accessibilityModule != null){
                        accessibilityModule.setEnabled(false);
                    }

                }

            }
        });

        overlaysButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEnableDrawOverlays();
            }
        });

        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toEnableAccessibility();
            }
        });

    }

    private void toEnableAccessibility() {
        if (!AccessibilityUtils.toEnableAccessibility(LayoutInspectorInfoActivity.this)) {
            Toast.makeText(LayoutInspectorInfoActivity.this, "请手动找到并允许该应用的辅助功能", Toast.LENGTH_LONG).show();
        }
    }

    private void toEnableDrawOverlays() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!DrawOverlaysUtils.toEnableDrawOverlays(LayoutInspectorInfoActivity.this)) {
                Toast.makeText(LayoutInspectorInfoActivity.this, "请手动找到并允许该应用在其他应用上叠加显示", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(LayoutInspectorInfoActivity.this, "安卓6.0以下无需申请叠加显示的权限", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //刷新显示
        refresh();
    }

    private void refresh(){
        //API是否支持
        boolean isApiMeetingRequirement = DeviceUtils.getVersionSDK() >= REQUIRED_API;
        //叠加层是否允许
        boolean canDrawOverlays = DrawOverlaysUtils.canDrawOverlays(this);
        //辅助功能是否允许
        boolean isServiceEnabled = AccessibilityUtils.isServiceEnabled(this, getPackageName());

        //功能是否开启
        LayoutInspectorAccessibilityModule accessibilityModule = AccessibilityContainerService.getModule(LayoutInspectorAccessibilityModule.class);
        boolean isModuleEnabled = false;
        if (accessibilityModule != null){
            isModuleEnabled = accessibilityModule.isEnabled();
        }

        //显示
        noticeView.setText("\nWARNING: \"Accessibility\" is a high risk permission. You should disable it after used by click \"" + getString(R.string.layout_inspector_info_main_accessibility_button) + "\" button.\n" +
                "\nIs device api level above " + REQUIRED_API + ": " + isApiMeetingRequirement +
                "\nIs draw overlays permission enabled: " + canDrawOverlays +
                "\nIs accessibility permission enabled: " + isServiceEnabled +
                "\nIs service module enabled: " + isModuleEnabled);
        //设置开关状态
        switchButton.setChecked(isModuleEnabled && isApiMeetingRequirement && canDrawOverlays && isServiceEnabled);
    }

    private MyHandler myHandler = new MyHandler(this);

    private static class MyHandler extends WeakHandler<LayoutInspectorInfoActivity>{

        private static final int REFRESH = 1;

        private MyHandler(LayoutInspectorInfoActivity host) {
            super(host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, LayoutInspectorInfoActivity host) {
            switch (msg.what){
                case REFRESH:
                    host.refresh();
                    break;
            }
        }

    }

}
