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

package sviolet.turquoise.enhance.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import sviolet.turquoise.common.statics.StringConstants;
import sviolet.turquoise.util.droid.ApplicationUtils;

/**
 * <p>[组件扩展]MultiDex加载专用Activity</p>
 *
 * <p>使用说明见{@link TApplicationForMultiDex}</p>
 *
 * Created by S.Violet on 2017/3/20.
 */
public abstract class MultiDexLoadingActivity extends Activity {

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        initViews(savedInstanceState);
        Log.i("TurquoiseMultiDex", "mini process: execute loading task");
        new LoadDexTask().execute();
    }

    protected void initViews(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(getLayoutId());
    }

    /**
     * @return 返回指定的加载界面布局ID
     */
    protected abstract int getLayoutId();

    /**
     * MultiDex加载任务(dexopt任务)
     */
    private class LoadDexTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                Log.i("TurquoiseMultiDex", "mini process: dex opt start");
                //利用第一次加载, 来执行dexopt
                MultiDex.install(getApplication());
                Log.i("TurquoiseMultiDex", "mini process: dex opt finish");

                //保存sha1和客户端版本
                SharedPreferences prefs = getApplication().getSharedPreferences(StringConstants.MULTI_DEX_PREF_NAME, MODE_MULTI_PROCESS);
                String recentSha1 = prefs.getString(StringConstants.MULTI_DEX_PREF_SHA1_KEY, "");
                String recentVersion = prefs.getString(StringConstants.MULTI_DEX_PREF_VERSION_KEY, "");

                prefs.edit()
                        .putString(StringConstants.MULTI_DEX_PREF_SHA1_KEY, TApplicationForMultiDex.get2thDexSHA1(getApplication()))
                        .putString(StringConstants.MULTI_DEX_PREF_VERSION_KEY, ApplicationUtils.getAppVersionName(getApplication()))
                        .commit();
            } catch (Exception e) {
                Log.e("TurquoiseMultiDex", "mini process: dex opt error", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            Log.i("TurquoiseMultiDex", "mini process: dex opt succeed");
            finish();
            System.exit(0);
        }

    }

    @Override
    public void onBackPressed() {
        //屏蔽返回键
    }

}
