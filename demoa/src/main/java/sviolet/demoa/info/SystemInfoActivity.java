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

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.TextView;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.utils.RuntimePermissionManager;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.FingerprintUtils;
import sviolet.turquoise.util.droid.SystemAppUtils;

/**
 * 系统信息
 */
@DemoDescription(
        title = "System Info",
        type = "Info",
        info = "System info"
)

@ResourceId(R.layout.system_info_main)
@ActivitySettings(
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class SystemInfoActivity extends TActivity {

    @ResourceId(R.id.system_info_main_textview)
    private TextView textView;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        executePermissionTask(new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_WIFI_STATE}, new RuntimePermissionManager.RequestPermissionTask() {
            @Override
            public void onResult(String[] permissions, int[] grantResults, boolean allGranted) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(getDeviceInfo());
                stringBuilder.append(getSystemInfo());
                stringBuilder.append(getCpuInfo());
                stringBuilder.append(getWebViewInfo());
                if (allGranted) {
                    stringBuilder.append(getIds());
                }
                textView.setText(stringBuilder.toString());
            }
        });
    }

    private String getDeviceInfo(){
        return "\nManufacturer: " + DeviceUtils.getProductManufacturer() +
                "\nBrand: " + DeviceUtils.getProductBrand() +
                "\nModel: " + DeviceUtils.getModel();
    }

    private String getSystemInfo(){
        return "\nBuild id: " + DeviceUtils.getBuildDisplayId() +
                "\nVersion sdk: " + DeviceUtils.getVersionSDK() +
                "\nVersion release: " + DeviceUtils.getVersionRelease() +
                "\nMemory class: " + DeviceUtils.getMemoryClass(this) +
                "\nFingerprint: " + FingerprintUtils.getDebugInfo(this);
    }

    private String getCpuInfo(){
        StringBuilder stringBuilder = new StringBuilder("\nCpu AIBS: ");
        String[] aibs = DeviceUtils.getCpuAbis();
        if (aibs != null){
            for (String aib : aibs){
                stringBuilder.append(aib);
                stringBuilder.append(" ");
            }
        }
        return stringBuilder.toString();
    }

    /**
     * WebView信息
     */
    private String getWebViewInfo() {
        SystemAppUtils.AndroidSystemWebViewInfo info = SystemAppUtils.getAndroidSystemWebViewInfo(this);
        if (info != null) {
            return "\nWebView version: " + info.versionName + "(" + info.versionCode + ")\nWebView is updated: " + info.isUpdated;
        }
        return "\nWebView version: null\nWebView is updated: null";
    }

    @SuppressLint("MissingPermission")
    private String getIds(){
        return "\nAndroid ID: " + DeviceUtils.getAndroidId(this) +
                "\nIMEI: " + DeviceUtils.getIMEI(this) +
                "\nMAC: " + DeviceUtils.getMacAddress(this);
    }

}
