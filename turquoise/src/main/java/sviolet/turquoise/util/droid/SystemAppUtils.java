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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

/**
 * 调用系统APP工具
 * Created by S.Violet on 2015/7/27.
 */
public class SystemAppUtils {

    /**
     * 打开联系人列表
     * @param context activity
     */
    public static void openContacts(Activity context) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        context.startActivity(intent);
    }

    /**
     * 打开联系人列表(startActivityForResult方式)
     * @param context activity
     * @param requestCode 请求吗
     */
    public static void openContactsForResult(Activity context, int requestCode){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 打开电话, 并拨打号码
     *
     * @param activity activity
     * @param number 电话号码
     */
    public static void openPhoneAndCall(Activity activity, String number) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    /**
     * 打开电话(不直接拨打)
     *
     * @param activity activity
     * @param number 电话号码
     */
    public static void openPhone(Activity activity, String number) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + number));
        activity.startActivity(intent);
    }

    /**
     * 打开浏览器
     *
     * @param activity
     * @param url
     */
    public static void openBrowser(Activity activity, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }

    /**
     * 获取系统自带WebView(Android System WebView)的信息
     * @param context context
     * @return 可为空, 如果没有Android System WebView
     */
    @Nullable
    public static AndroidSystemWebViewInfo getAndroidSystemWebViewInfo(@NonNull Context context){
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packageInfoList = packageManager.getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList) {
            if ("com.google.android.webview".equals(packageInfo.packageName)) {
                AndroidSystemWebViewInfo info = new AndroidSystemWebViewInfo();
                info.versionName = packageInfo.versionName;
                info.versionCode = packageInfo.versionCode;
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) > 0) {
                    info.isUpdated = true;
                } else {
                    info.isUpdated = false;
                }
                return info;
            }
        }
        return null;
    }

    public static class AndroidSystemWebViewInfo {

        /**
         * WebView版本
         */
        public String versionName;

        /**
         * WebView版本(数字)
         */
        public int versionCode;

        /**
         * true:用户升级过系统WebView, 一般系统WebView不可以升级, 升级会与系统不兼容
         * false:用户未升级过系统WebView, 系统自带
         */
        public boolean isUpdated;
    }

}
