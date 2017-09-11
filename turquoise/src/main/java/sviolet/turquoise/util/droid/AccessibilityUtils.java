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

package sviolet.turquoise.util.droid;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.view.accessibility.AccessibilityManager;

import java.util.Collections;
import java.util.List;

/**
 * 安卓"辅助(无障碍)功能"工具
 *
 * Created by S.Violet on 2017/9/8.
 */
public class AccessibilityUtils {

    /**
     * 列出所有安装的申明了辅助功能的服务(包括未开启的)
     * @param context context
     */
    public static List<AccessibilityServiceInfo> listInstalledServices(@NonNull Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            return Collections.emptyList();
        }
        return accessibilityManager.getInstalledAccessibilityServiceList();
    }

    /**
     * 列出所有启用的辅助功能服务
     * @param context context
     */
    public static List<AccessibilityServiceInfo> listEnabledServices(@NonNull Context context) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager == null) {
            return Collections.emptyList();
        }
        return accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
    }

    /**
     * 判断指定包名的辅助服务是否被开启
     * @param context context
     * @param servicePackageName 声明辅助服务的包名
     */
    public static boolean isServiceEnabled(@NonNull Context context, @NonNull String servicePackageName){
        servicePackageName = servicePackageName + "/";
        List<AccessibilityServiceInfo> list = listEnabledServices(context);
        for (AccessibilityServiceInfo info : list){
            if (info != null && info.getId() != null && info.getId().startsWith(servicePackageName)){
                return true;
            }
        }
        return false;
    }

    /**
     * 判断指定包名的APP是否被专门监听. 如果AccessibilityService配置的packageNames为空, 即监听全部的APP, 不视为被专门监听.
     * @param context context
     * @param appPackageName APP的包名
     * @return true:被专门监听
     */
    public static boolean isAppMonitored(@NonNull Context context, @NonNull String appPackageName){
        List<AccessibilityServiceInfo> list = listEnabledServices(context);
        for (AccessibilityServiceInfo info : list){
            if (info != null && info.packageNames != null){
                for (String packageName : info.packageNames){
                    if (appPackageName.equals(packageName)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 通过该方法引导用户开启辅助(无障碍)权限
     * @param context context
     * @return true:找到并打开了设置界面 false:找不到设置界面
     */
    public static boolean toEnableAccessibility(@NonNull Context context){
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException exception){
            return false;
        }
    }

}
