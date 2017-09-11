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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

/**
 * 覆盖层绘制工具(悬浮窗, 即允许在其他APP上方显示的窗口)
 *
 * Created by S.Violet on 2017/9/11.
 */
public class DrawOverlaysUtils {

    /**
     * 判断是否有权限绘制覆盖层, 6.0以上需要用户手动开启权限
     * @param context context
     * @return true:允许
     */
    @RequiresPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
    public static boolean canDrawOverlays(@NonNull Context context) {
        //API23以上需要判断权限
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    /**
     * 6.0以上系统通过该方法请求用户开启覆盖层权限(打开系统设置界面)
     * @param context context
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void toEnableDrawOverlays(@NonNull Context context){
        Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

}
