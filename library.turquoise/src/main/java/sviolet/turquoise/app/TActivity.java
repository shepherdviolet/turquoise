package sviolet.turquoise.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;

import sviolet.turquoise.annotation.ActivitySettings;
import sviolet.turquoise.annotation.ResourceId;
import sviolet.turquoise.utils.DeviceUtils;
import sviolet.turquoise.utils.SettingUtils;

/**
 * [组件扩展]Activity<br>
 * <br>
 * 1.Activity布局文件注入<br>
 * 注入Activity@ResourceId注释对应的布局文件<br>
 * <br>
 * 2.成员View对象注入<br>
 * 注入带@ResourceId注释的成员View对象<br>
 * <br>
 * 3.Activity注释式设置<br>
 * @see sviolet.turquoise.annotation.ActivitySettings;
 *
 * @author S.Violet
 */

public abstract class TActivity extends Activity {

    private Logger mLogger;//日志打印器

    private ActivitySettings settings;

    private int activityIndex;//对应在TApplication中的编号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        windowSetting();//窗口设置
        super.onCreate(savedInstanceState);
        injectContentView();// 注入Activity布局
        injectViewId(getClass());// 注入成员View

        //将自身加入TApplication
        if (getApplication() instanceof TApplication){
           try {
               activityIndex = ((TApplication) getApplication()).addActivity(this);
           }catch (Exception ignored){}
        }
    }

    /**
     * 获得@ActivitySettings设置标签
     *
     * @return
     */
    private ActivitySettings getActivitySettings() {
        if (settings == null) {
            if (this.getClass().isAnnotationPresent(ActivitySettings.class)) {
                settings = this.getClass().getAnnotation(ActivitySettings.class);
            }
        }
        return settings;
    }

    /**
     * 根据@ActivitySettings标签进行窗口设置
     */
    @SuppressLint("NewApi")
    private void windowSetting() {
        if (getActivitySettings() == null)
            return;

        //硬件加速
        if(getActivitySettings().enableHardwareAccelerated()){
            SettingUtils.enableHardwareAccelerated(getWindow());
        }

        //无标题
        if (getActivitySettings().noTitle()) {
            getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        //5.0效果
        if (DeviceUtils.getVersionSDK() >= Build.VERSION_CODES.LOLLIPOP) {
            //透明状态栏/底部按钮, 最大化Activity
            if (getActivitySettings().translucentBar()) {
                getWindow().clearFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                                | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
                getWindow().addFlags(
                        WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
            //状态栏颜色
            if (getActivitySettings().statusBarColor() != ActivitySettings.DEF_STATUS_BAR_COLOR) {
                getWindow().setStatusBarColor(getActivitySettings().statusBarColor());
            }
            //底部按钮颜色
            if (getActivitySettings().navigationBarColor() != ActivitySettings.DEF_NAVIGATION_BAR_COLOR) {
                getWindow().setNavigationBarColor(getActivitySettings().navigationBarColor());
            }
        }
    }

    /**
     * 根据Activity的@OptionsMenuId标签, 注入OptionsMenu菜单布局文件<br>
     * 只需复写onOptionsItemSelected方法截获事件即可<br>
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getActivitySettings() != null) {
            int optionsMenuId = getActivitySettings().optionsMenuId();
            if (optionsMenuId != ActivitySettings.DEF_OPTIONS_MENU_ID) {
                getMenuInflater().inflate(optionsMenuId, menu);
                return true;
            }
        }
        return false;
    }

    /**
     * 根据Activity的@ResourceId标签, 注入Activity的布局文件
     */
    protected void injectContentView() {
        if (this.getClass().isAnnotationPresent(ResourceId.class)) {
            try {
                int layoutId = this.getClass().getAnnotation(ResourceId.class).value();
                super.setContentView(layoutId);
            } catch (Exception e) {
                throw new InjectException("[TActivity]inject ContentView failed", e);
            }
        }
    }

    /**
     * 根据Activity中成员变量的@ResourceId标签, 注入对应ID的View对象
     */
    protected void injectViewId(Class<?> clazz) {

        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields)
            if (field.isAnnotationPresent(ResourceId.class))
                injectView(field);

        Class superClazz = clazz.getSuperclass();
        if (!TActivity.class.equals(superClazz)) {
            injectViewId(superClazz);
        }
    }

    /**
     * 根据field的标签注入View对象
     *
     * @param field field
     */
    private void injectView(Field field) {
        try {
            int resourceId = field.getAnnotation(ResourceId.class).value();
            View view = findViewById(resourceId);
            if (view == null)
                throw new InjectException("[TActivity]inject view [" + field.getName() + "] failed, can't find resource");
            field.setAccessible(true);
            field.set(this, view);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new InjectException("[TActivity]inject view [" + field.getName() + "] failed", e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //将自身从TApplication移除
        if (getApplication() instanceof TApplication){
            try {
                ((TApplication) getApplication()).removeActivity(activityIndex);
            }catch (Exception ignored){}
        }
    }

    /**********************************************
     * public
     */

    /**
     * 获得日志打印器(需配合TApplication)<br/>
     * 由TApplication子类标签设置日志打印权限<br/>
     * 若本应用不采用TApplication, 则返回的日志打印器无用.<br/>
     *
     */
    public Logger getLogger(){
        if (getApplication() instanceof TApplication){
            try {
                return ((TApplication) getApplication()).getLogger();
            }catch (Exception ignored){}
        }
        return new Logger("", false, false, false);//返回无效的日志打印器
    }
}
