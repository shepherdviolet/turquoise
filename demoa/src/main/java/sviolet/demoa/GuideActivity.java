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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import sviolet.demoa.common.DemoDefault;
import sviolet.demoa.common.DemoList;
import sviolet.demoa.common.DemoListAdapter;
import sviolet.demoa.image.ImageActivity;
import sviolet.demoa.slide.SlideActivity;
import sviolet.turquoise.enhance.app.TActivity;
import sviolet.turquoise.enhance.app.annotation.inject.ResourceId;
import sviolet.turquoise.enhance.app.annotation.setting.ActivitySettings;
import sviolet.turquoise.modelx.bitmaploader.BitmapLoader;

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
        ImageActivity.class,
        TempActivity.class
})

/**************************************************************
 *  Activity
 */

@ResourceId(R.layout.guide_main)
@ActivitySettings(
        optionsMenuId = R.menu.menu_guide,
        noTitle = false,
        translucentBar = false,
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
)
public class GuideActivity extends TActivity {

    @ResourceId(R.id.guide_main_listview)
    private ListView demoListView;
    private DemoListAdapter demoListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectDemoListView();
        injectDefaultDemo();
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
        } else if (id == R.id.guide_menu_wipe_cache){
            /*
                清空磁盘缓存, 此处为简易写法, 为保证流畅, 应另启线程处理,
                同时显示进度条遮罩, 阻止用户操作. 要确保该方法执行时, 对
                应磁盘缓存区无读写操作, 否则会抛出异常.
            */
            try {
                BitmapLoader.wipeDiskCache(this, "AsyncImageActivity");//若有外部储存, 清除外部缓存, 否则清除内部缓存
//                BitmapLoader.wipeInnerDiskCache(this, "AsyncImageActivity");//强制清除内部储存的缓存
                Toast.makeText(getApplicationContext(), "cache wipe complete", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "cache wipe failed", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
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
        if (activity == null)
            return;
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
                    if (view != null)
                        go((Class<? extends Activity>) demoListAdapter.getItem(position));
                }
            });
        }
    }
}
