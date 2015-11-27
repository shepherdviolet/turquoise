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

package sviolet.turquoise.enhanced;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import sviolet.turquoise.enhanced.annotation.setting.ActivitySettings;
import sviolet.turquoise.enhanced.utils.InjectUtils;
import sviolet.turquoise.utils.Logger;

/**
 * [组件扩展]Activity<br>
 * <br>
 * 0.Activity注释式设置<br/>
 * {@link ActivitySettings};<br/>
 * <br/>
 * 1.InjectUtils注释式注入控件对象/绑定监听<br/>
 * {@link InjectUtils};<br/>
 * <br>
 *
 * @author S.Violet
 */
public class TFragmentActivity extends FragmentActivity{

    private static final TActivityProvider provider = new TActivityProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        provider.windowSetting(this);//窗口设置
        super.onCreate(savedInstanceState);
        provider.onCreate(this);
    }

    /**
     * 根据Activity的@OptionsMenuId标签, 注入OptionsMenu菜单布局文件<br>
     * 只需复写onOptionsItemSelected方法截获事件即可<br>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return provider.onCreateOptionsMenu(menu, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        provider.onDestroy(this);
    }

    /**********************************************
     * public
     *
     * Utils: Logger / BitmapUtils
     */

    /**
     * 获得日志打印器(需配合TApplication)<br/>
     * 由TApplication子类标签设置日志打印权限<br/>
     * 若本应用不采用TApplication, 则返回的日志打印器无用.<br/>
     *
     */
    public Logger getLogger(){
        return provider.getLogger(this);
    }

}
