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

package sviolet.demoa.info.utils;

import android.graphics.PixelFormat;
import android.support.annotation.RequiresApi;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import sviolet.demoa.R;
import sviolet.demoa.info.LayoutInspectorInfoActivity;
import sviolet.turquoise.util.droid.DrawOverlaysUtils;
import sviolet.turquoise.utilx.accessibility.AccessibilityContainerService;
import sviolet.turquoise.utilx.accessibility.AccessibilityModule;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 布局分析辅助服务模块
 *
 * Created by S.Violet on 2017/9/12.
 */
//设置最低版本要求
@AccessibilityModule.Api(LayoutInspectorInfoActivity.REQUIRED_API)
public class LayoutInspectorAccessibilityModule extends AccessibilityModule {

    private TLogger logger = TLogger.get(this);

    private boolean logEnabled = false;//是否开启日志
    private boolean enabled = true;//是否启用分析

    private View floatingView;//悬浮窗
    private LayoutInspectorView inspectorView;//悬浮窗中的布局显示控件
    private WindowManager.LayoutParams windowLayoutParams;//窗口参数

    @RequiresApi(LayoutInspectorInfoActivity.REQUIRED_API)
    protected LayoutInspectorAccessibilityModule(AccessibilityContainerService service) {
        super(service);
    }

    @Override
    protected boolean onLowApi() {
        //当设备版本过低时, 不启动该模块
        return false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        initAccessibilityService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        enabled = false;//停止分析
        destroyFloatingWindow();//销毁悬浮窗
    }

    @Override
    @RequiresApi(LayoutInspectorInfoActivity.REQUIRED_API)
    protected void onAccessibilityEvent(AccessibilityEvent event) {

        //根据状态创建或销毁悬浮窗
        refreshFloatingWindow();

        if (!enabled){
            return;
        }

        if (logEnabled) {
            logger.d("---------------------------------------");
            logger.d("AccessibilityEvent:" + event);
            logger.d("---------------------------------------");
        }

        //获取根窗口
        AccessibilityNodeInfo accessibilityNodeInfo = getService().getRootInActiveWindow();
        if (accessibilityNodeInfo == null) {
            if (logEnabled) {
                logger.d("AccessibilityNodeInfo is null");
            }
            return;
        }

        //解析布局数据
        String prefix = "";
        LayoutInspectorNodeInfo nodeInfo = parseNode(accessibilityNodeInfo, prefix);

        //刷新显示
        LayoutInspectorView inspectorView = this.inspectorView;
        if (inspectorView != null){
            inspectorView.refreshNodeInfo(nodeInfo);
        }

    }

    @Override
    protected void onInterrupt() {

    }

    private void initAccessibilityService(){
//        AccessibilityServiceInfo serviceInfo = getServiceInfo();
//        serviceInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
//        serviceInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        serviceInfo.notificationTimeout = 100;
//        serviceInfo.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
//                AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS |
//                AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY |
//                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
//                AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
//        serviceInfo.packageNames = new String[]{
//                "sviolet.demoa",
//                "sviolet.demoaimageloader"
//        };
//        setServiceInfo(serviceInfo);
    }

    private void refreshFloatingWindow(){
        if (enabled) {
            if (floatingView == null) {
                //创建悬浮窗
                createFloatingWindow();
            }
        } else {
            if (floatingView != null) {
                //销毁悬浮窗
                destroyFloatingWindow();
            }
        }
    }

    /**
     * 创建悬浮窗
     */
    private void createFloatingWindow() {
        if (!DrawOverlaysUtils.canDrawOverlays(getService())){
            enabled = false;
            return;
        }
        floatingView = LayoutInflater.from(getService()).inflate(R.layout.info_layout_inspector_floating_window, null);
        inspectorView = (LayoutInspectorView) floatingView.findViewById(R.id.info_layout_inspector_floating_window_view);
        View dragView = floatingView.findViewById(R.id.info_layout_inspector_floating_window_button);

        windowLayoutParams = new WindowManager.LayoutParams();
        windowLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        windowLayoutParams.format = PixelFormat.RGBA_8888;
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        windowLayoutParams.gravity = Gravity.START | Gravity.TOP;
        windowLayoutParams.x = 0;
        windowLayoutParams.y = 0;
        windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

        DrawOverlaysUtils.addOverlays(getService(), floatingView, dragView, false, windowLayoutParams);

        dragView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (inspectorView.getVisibility() == View.VISIBLE) {
                    inspectorView.setVisibility(View.GONE);
                    windowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
                    windowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
                    DrawOverlaysUtils.updateOverlays(getService(), floatingView, windowLayoutParams);
                } else {
                    inspectorView.setVisibility(View.VISIBLE);
                    windowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
                    windowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
                    DrawOverlaysUtils.updateOverlays(getService(), floatingView, windowLayoutParams);
                }
            }
        });
    }

    /**
     * 销毁悬浮窗
     */
    private void destroyFloatingWindow(){
        DrawOverlaysUtils.removeOverlays(getService(), floatingView);
        floatingView = null;
        inspectorView = null;
        windowLayoutParams = null;
    }

    /**
     * 解析布局数据
     */
    @RequiresApi(LayoutInspectorInfoActivity.REQUIRED_API)
    private LayoutInspectorNodeInfo parseNode(AccessibilityNodeInfo accessibilityNodeInfo, String prefix) {
        LayoutInspectorNodeInfo nodeInfo = LayoutInspectorNodeInfo.obtain();
        nodeInfo.setId(accessibilityNodeInfo.getViewIdResourceName());
        nodeInfo.setClazz(accessibilityNodeInfo.getClassName().toString());
        accessibilityNodeInfo.getBoundsInScreen(nodeInfo.getRect());
        if (logEnabled) {
            logger.d(prefix + accessibilityNodeInfo.getViewIdResourceName() + " " + nodeInfo.getRect());
        }
        if (accessibilityNodeInfo.getChildCount() > 0) {
            prefix = prefix + "+";
            for (int i = 0; i < accessibilityNodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo subAccessibilityNodeInfo = accessibilityNodeInfo.getChild(i);
                if (subAccessibilityNodeInfo == null){
                    continue;
                }
                LayoutInspectorNodeInfo subNodeInfo = parseNode(subAccessibilityNodeInfo, prefix);
                nodeInfo.getSubs().add(subNodeInfo);
            }
        }
        accessibilityNodeInfo.recycle();
        return nodeInfo;
    }

    /**
     * 设置是否允许日志打印
     * @param logEnabled 默认false
     */
    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    /**
     * 启用布局分析
     * @param enabled 默认true
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 判断当前是否启用了布局分析
     * @return
     */
    public boolean isEnabled() {
        return enabled;
    }

}
