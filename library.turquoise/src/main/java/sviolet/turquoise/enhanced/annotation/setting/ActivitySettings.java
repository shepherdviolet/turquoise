package sviolet.turquoise.enhanced.annotation.setting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * [TActivity注释]Activity设置
 * <p/>
 *
     @ActivitySettings(
        optionsMenuId = R.menu.menu_guide,
        noTitle = false,
        translucentBar = false,
        statusBarColor = 0xFF209090,
        navigationBarColor = 0xFF209090
     )
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
     * <p/>
     * public boolean onOptionsItemSelected(MenuItem item) {
     *      int id = item.getItemId();
     *      if (id == R.id.guide_menu_settings) {
     *          return true;
     *      }
     *      return super.onOptionsItemSelected(item);
     * }
     *
     */
    int optionsMenuId() default DEF_OPTIONS_MENU_ID;

    /**
     * Activity无标题
     */
    boolean noTitle() default false;

    /**
     * 5.0
     * 状态栏/底部按钮透明, 并最大化Activity撑满屏幕
     */
    boolean translucentBar() default false;

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
