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

package sviolet.turquoise.enhance.app.annotation.setting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [TActivity注释]Activity设置
 * <p/>
 *
 * <pre>{@code
 *      @ActivitySettings(
 *      optionsMenuId = R.menu.menu_guide,
 *      noTitle = false,
 *      translucentBar = false,
 *      statusBarColor = 0xFF30C0C0,
 *      navigationBarColor = 0xFF30C0C0
 *      )
 * }</pre>
 *
 * @author S.Violet
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ActivitySettings {

    int DEF_OPTIONS_MENU_ID = 0;
    int DEF_STATUS_BAR_COLOR = 0xFF000000;
    int DEF_NAVIGATION_BAR_COLOR = 0xFF000000;

    /**
     * OptionsMenu菜单布局ID
     * <p/>
     * 标注注释后, 复写如下方法:
     *
     * <pre>{@code
     *  public boolean onOptionsItemSelected(MenuItem item) {
     *       int id = item.getItemId();
     *       if (id == R.id.guide_menu_settings) {
     *           return true;
     *       }
     *       return super.onOptionsItemSelected(item);
     *  }
     * }</pre>
     *
     */
    int optionsMenuId() default DEF_OPTIONS_MENU_ID;

    /**
     * Activity无标题
     */
    boolean noTitle() default false;

    /**
     * 5.0
     * 状态栏透明, 并最大化Activity撑满屏幕
     */
    boolean translucentStatus() default false;

    /**
     * 5.0
     * 底部按钮透明, 并最大化Activity撑满屏幕
     */
    boolean translucentNavigation() default false;

    /**
     * 6.0
     * 状态栏ICON深色
     */
    boolean lightStatusIcon() default false;

    /**
     * 5.0
     * 状态栏颜色
     */
    int statusBarColor() default DEF_STATUS_BAR_COLOR;

    /**
     * 5.0
     * 底部按钮颜色
     */
    int navigationBarColor() default DEF_NAVIGATION_BAR_COLOR;

    /**
     * 启用硬件加速
     */
    boolean enableHardwareAccelerated() default false;

}
