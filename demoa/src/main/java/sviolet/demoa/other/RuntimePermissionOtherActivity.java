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

package sviolet.demoa.other;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import sviolet.demoa.R;
import sviolet.demoa.common.DemoDescription;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.utils.RuntimePermissionManager;

@DemoDescription(
        title = "Runtime Permission Demo",
        type = "Other",
        info = "Runtime Permission Demo"
)

/**
 * 运行时权限示例<br/>
 *
 * Created by S.Violet on 2015/7/7.
 */
@ResourceId(R.layout.other_runtime_permission)
@ActivitySettings(
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class RuntimePermissionOtherActivity extends TActivity {

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        executePermissionTask(Manifest.permission.WRITE_EXTERNAL_STORAGE, "Storage permission", "We need to write external storage, if you want to xxxx you should allow this permission", new RuntimePermissionManager.RequestPermissionTask() {
            @Override
            public void onResult(String[] permissions, int[] grantResults, boolean allGranted) {
                if (allGranted) {
                    Toast.makeText(RuntimePermissionOtherActivity.this, "external permission request succeed!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(RuntimePermissionOtherActivity.this, "external permission request failed!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
