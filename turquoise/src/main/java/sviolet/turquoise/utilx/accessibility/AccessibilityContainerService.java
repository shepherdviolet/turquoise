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

package sviolet.turquoise.utilx.accessibility;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;

import sviolet.turquoise.util.droid.DeviceUtils;

/**
 * <p>辅助功能服务容器, 可装载多个辅助功能模块</p>
 * <p>
 * <p>使用方法:</p>
 * <p>
 * <p>1.在AccessibilityModule抽象类中实现辅助功能逻辑</p>
 * <p>
 * <p>2.在AndroidManifest.xml中配置服务, 在meta-data:modules中配置需要加载的模块(多个模块用逗号,分隔)</p>
 * <p>
 * <pre>{@code
 *      <service
 *          android:name="sviolet.turquoise.utilx.accessibility.AccessibilityContainerService"
 *          android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
 *          android:enabled="true"
 *          android:exported="true">
 *          <intent-filter>
 *              <action android:name="android.accessibilityservice.AccessibilityService"/>
 *          </intent-filter>
 *          <meta-data
 *              android:name="android.accessibilityservice"
 *              android:resource="@xml/accessibility" />
 *          <meta-data
 *              android:name="modules"
 *              android:value="sviolet.demoa.info.utils.LayoutInspectorAccessibilityModule,sviolet.demoa.info.utils.OtherAccessibilityModule" />
 *      </service>
 * }</pre>
 * <p>
 * <p>3.在res/xml中新建配置文件accessibility.xml,
 * 配置参考https://developer.android.google.cn/reference/android/accessibilityservice/AccessibilityServiceInfo.html</p>
 * <p>
 * <pre>{@code
 *      <?xml version="1.0" encoding="utf-8"?>
 *      <accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
 *          android:accessibilityEventTypes="typeAllMask"
 *          android:accessibilityFeedbackType="feedbackGeneric"
 *          android:accessibilityFlags="flagDefault|flagReportViewIds"
 *          android:canRetrieveWindowContent="true"
 *          android:notificationTimeout="100"
 *          android:packageNames="sviolet.demoa,sviolet.demoaimageloader"
 *          android:description="@string/accessibility_description"/>
 * }</pre>
 * <p>
 * <p>4.使用AccessibilityUtils检查权限/引导用户打开权限, 权限开始时, 服务就会被启用</p>
 * <p>
 * Created by S.Violet on 2017/9/11.
 */
public final class AccessibilityContainerService extends AccessibilityService {

    private static AccessibilityContainerService INSTANCE;//实例

    private ConcurrentHashMap<Class<?>, AccessibilityModule> modules = new ConcurrentHashMap<>();//模块
    private int eventTypesBackup = 0;//备份配置中的事件类型

    @Override
    public void onCreate() {
        super.onCreate();

        //静态持有实例
        INSTANCE = this;

        //从meta-data中获取要加载的模块类名
        String[] moduleNameArray;
        try {
            //获取meta-data
            ServiceInfo info = getPackageManager().getServiceInfo(new ComponentName(this, AccessibilityContainerService.class), PackageManager.GET_META_DATA);
            //获取modules字段
            String moduleNames = info.metaData.getString("modules");
            if (moduleNames == null) {
                throw new IllegalArgumentException("[AccessibilityContainerService]Missing meta-data \"modules\" in service declaration in AndroidManifest.xml");
            }
            //删除空格
            moduleNames = moduleNames.replaceAll(" ", "");
            if (moduleNames.length() <= 0) {
                throw new IllegalArgumentException("[AccessibilityContainerService]Empty meta-data \"modules\" in service declaration in AndroidManifest.xml");
            }
            //根据分隔符切开
            moduleNameArray = moduleNames.split(",");
        } catch (Exception e) {
            throw new IllegalArgumentException("[AccessibilityContainerService]Illegal meta-data \"modules\" in service declaration in AndroidManifest.xml", e);
        }

        if (moduleNameArray.length <= 0) {
            throw new IllegalArgumentException("[AccessibilityContainerService]Illegal meta-data \"modules\" in service declaration in AndroidManifest.xml, size 0");
        }

        //装载模块
        for (String moduleName : moduleNameArray) {
            try {
                //加载类
                Class<?> clazz = Class.forName(moduleName);
                //模块不会重复加载
                if (modules.containsKey(clazz)) {
                    continue;
                }
                //实例化
                Constructor<?> constructor = clazz.getDeclaredConstructor(AccessibilityContainerService.class);
                AccessibilityModule module = (AccessibilityModule) constructor.newInstance(this);
                //判断API版本
                if (clazz.isAnnotationPresent(AccessibilityModule.Api.class)) {
                    AccessibilityModule.Api api = clazz.getAnnotation(AccessibilityModule.Api.class);
                    if (DeviceUtils.getVersionSDK() < api.value()) {
                        if (!module.onLowApi()) {
                            continue;
                        }
                    }
                }
                //加入模块
                modules.put(clazz, module);
            } catch (Exception e) {
                throw new IllegalArgumentException("[AccessibilityContainerService]Illegal meta-data \"modules\" in service declaration in AndroidManifest.xml, create module class " + moduleName + " failed", e);
            }
        }

    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        for (AccessibilityModule module : modules.values()) {
            module.onServiceConnected();
        }

        if (android.os.Build.VERSION.SDK_INT >= 16) {
            AccessibilityServiceInfo serviceInfo = getServiceInfo();
            eventTypesBackup = serviceInfo.eventTypes;//备份事件类型
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (AccessibilityModule module : modules.values()) {
            module.onDestroy();
        }
        //清空模块
        modules.clear();
        //释放实例
        INSTANCE = null;
    }

    @Override
    protected boolean onGesture(int gestureId) {
        boolean result = false;
        for (AccessibilityModule module : modules.values()) {
            if (module.onGesture(gestureId)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        boolean result = false;
        for (AccessibilityModule module : modules.values()) {
            if (module.onKeyEvent(event)) {
                result = true;
            }
        }
        return result;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        for (AccessibilityModule module : modules.values()) {
            module.onAccessibilityEvent(event);
        }
    }

    @Override
    public void onInterrupt() {
        for (AccessibilityModule module : modules.values()) {
            module.onInterrupt();
        }
    }

    void removeModule(AccessibilityModule module) {
        if (module == null) {
            return;
        }
        modules.remove(module.getClass());
    }

    /**
     * 获取模块实例, 可能会返回空
     *
     * @param moduleType 模块类型
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T getModule(Class<T> moduleType) {
        AccessibilityContainerService service = INSTANCE;
        if (service == null) {
            return null;
        }
        return (T) service.modules.get(moduleType);
    }

    /**
     * [API16]全局开启/禁用辅助服务(通过修改监听的事件类型实现)
     * @param enabled true:启用 false:禁用
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setEnabled(boolean enabled){
        AccessibilityContainerService service = INSTANCE;
        if (service == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT < 16) {
            throw new RuntimeException("This method require API 16");
        }
        if (enabled){
            AccessibilityServiceInfo serviceInfo = service.getServiceInfo();
            serviceInfo.eventTypes = service.eventTypesBackup;
            service.setServiceInfo(serviceInfo);
        } else {
            AccessibilityServiceInfo serviceInfo = service.getServiceInfo();
            serviceInfo.eventTypes = 0x00000000;
            service.setServiceInfo(serviceInfo);
        }
    }

}
