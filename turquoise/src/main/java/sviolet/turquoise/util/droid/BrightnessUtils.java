/*
 * Copyright (C) 2015-2016 S.Violet
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

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;

/**
 * <p>屏幕亮度工具</p>
 *
 * Created by S.Violet on 2016/6/6.
 */
public class BrightnessUtils {

    /**
     * 设置当前Window的屏幕亮度
     *
     * @param activity activity
     * @param brightness 亮度[0, 1]
     */
    public static void setWindowBrightness(Activity activity, float brightness){
        Window window = activity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = brightness;
        window.setAttributes(layoutParams);
    }

    /**
     * 重置当前Window的屏幕亮度(变为全局值)
     *
     * @param activity activity
     */
    public static void resetWindowBrightness(Activity activity){
        setWindowBrightness(activity, -1);
    }

    /**
     * 获取屏幕亮度(全局)
     *
     * @param context context
     * @return 亮度[0, 255]
     */
    public static int getGlobalBrightness(Context context){
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception ignored) {
        }
        return -1;
    }

    /**
     * <p>设置屏幕亮度(全局), 可能需要配合设置手动亮度</p>
     *
     * <p>需要权限:android.permission.WRITE_SETTINGS</p>
     *
     * @param context context
     * @param brightness 亮度[0, 255]
     */
    public static void setGlobalBrightness(Context context, int brightness){
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Settings.System.getUriFor("screen_brightness");
        android.provider.Settings.System.putInt(contentResolver, "screen_brightness", brightness);
        contentResolver.notifyChange(uri, null);
    }

    /**
     * 判断当前屏幕亮度模式是否是自动模式
     *
     * @param context context
     * @return true:自动
     */
    public static boolean isGlobalBrightnessAutoMode(Context context){
        try{
            return Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
        } catch (Settings.SettingNotFoundException ignored) {
        }
        return false;
    }

    /**
     * <p>设置当前屏幕亮度模式</p>
     *
     * <p>需要权限:android.permission.WRITE_SETTINGS</p>
     *
     * @param context context
     * @param automatic true:自动 false:手动
     */
    public static void setGlobalBrightnessAutoMode(Context context, boolean automatic){
        Settings.System.putInt(context.getContentResolver(),
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                automatic ? Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC : Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }

}
