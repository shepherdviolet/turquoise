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

package sviolet.demoa;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

import sviolet.demoa.common.DemoDefault;
import sviolet.demoa.common.DemoList;
import sviolet.demoa.common.DemoListAdapter;
import sviolet.demoa.fingerprint.FingerprintActivity;
import sviolet.demoa.info.InfoActivity;
import sviolet.demoa.other.OtherActivity;
import sviolet.demoa.refresh.RefreshActivity;
import sviolet.demoa.slide.SlideActivity;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhance.app.utils.RuntimePermissionManager;
import sviolet.turquoise.ui.util.motion.MultiClickFilter;
import sviolet.turquoise.util.droid.DirectoryUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;
import sviolet.turquoise.utilx.tlogger.printer.SimpleLoggerPrinter;

/**************************************************************
 * Demo配置
 */
//默认Demo
//@DemoDefault(
//        TempActivity.class
//)

// Demo列表
@DemoList({
        SlideActivity.class,
        RefreshActivity.class,
        FingerprintActivity.class,
        InfoActivity.class,
        OtherActivity.class,
        TempActivity.class
})

/**************************************************************
 *  Activity
 */

@ResourceId(R.layout.guide_main)
@ActivitySettings(
        optionsMenuId = R.menu.menu_guide,
        noTitle = false,
        statusBarColor = 0xFF30C0C0,
        navigationBarColor = 0xFF30C0C0
)
public class GuideActivity extends TActivity {

    @ResourceId(R.id.guide_main_listview)
    private ListView demoListView;
    private DemoListAdapter demoListAdapter;

    private MultiClickFilter multiClickFilter = new MultiClickFilter();

    private static boolean loggerPrinterInitialized = false;

    @Override
    protected void onInitViews(Bundle savedInstanceState) {
        injectDemoListView();
        injectDefaultDemo();
        initLoggerPrinter();
    }

    private void initLoggerPrinter() {
        if (!loggerPrinterInitialized){
            loggerPrinterInitialized = true;
            executePermissionTask(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    null,
                    "We need permission to write log to disk",
                    new RuntimePermissionManager.RequestPermissionTask() {
                        @Override
                        public void onResult(String[] permissions, int[] grantResults, boolean allGranted) {
                            if (allGranted) {
                                File dataDir = DirectoryUtils.getExternalFilesDir(GuideActivity.this);
                                if (dataDir == null){
                                    Toast.makeText(GuideActivity.this, "No external disk in your device", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                File logDir = new File(dataDir.getAbsolutePath() + "/logs");
                                TLogger.setLoggerPrinter(new SimpleLoggerPrinter(logDir, 5, true));
                            } else {
                                Toast.makeText(GuideActivity.this, "No permission to write log to disk", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 菜单
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.guide_menu_settings) {
            return true;
        } else if (id == R.id.guide_menu_about) {
            //版本显示
            Toast.makeText(this, "Turquoise Demo " + BuildConfig.VERSION_NAME, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**************************************************************
     * private
     */

    /**
     * 进入指定的Activity
     *
     * @param activity 指定的Acitivity
     */
    private void go(Class<? extends Activity> activity) {
        if (activity == null) {
            return;
        }
        Intent intent = new Intent(this, activity);
        startActivity(intent);
    }

    /**
     * 注入并进入默认Demo
     */
    private void injectDefaultDemo() {
        if (this.getClass().isAnnotationPresent(DemoDefault.class)) {
            Class<? extends Activity> activity = this.getClass().getAnnotation(DemoDefault.class).value();
            go(activity);
        }
    }

    /**
     * 注入并显示Demo列表
     */
    private void injectDemoListView() {
        if (this.getClass().isAnnotationPresent(DemoList.class)) {
            Class<? extends Activity>[] activityList = this.getClass().getAnnotation(DemoList.class).value();
            demoListAdapter = new DemoListAdapter(this, R.layout.guide_main_item, activityList);
            demoListView.setAdapter(demoListAdapter);
            demoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (view != null) {
                        if (!multiClickFilter.tryHandle()){
                            return;
                        }
                        go((Class<? extends Activity>) demoListAdapter.getItem(position));
                    }
                }
            });
        }
    }
}
