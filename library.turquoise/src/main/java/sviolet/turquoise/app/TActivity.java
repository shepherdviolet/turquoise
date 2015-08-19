package sviolet.turquoise.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import sviolet.turquoise.annotation.app.ActivitySettings;
import sviolet.turquoise.utils.CachedBitmapUtils;
import sviolet.turquoise.utils.ApplicationUtils;
import sviolet.turquoise.utils.DeviceUtils;
import sviolet.turquoise.utils.InjectUtils;

/**
 * [组件扩展]Activity<br>
 * <br>
 * 0.Activity注释式设置<br/>
 * @see ActivitySettings ;
 * <br/>
 * 1.InjectUtils注释式注入控件对象/绑定监听<br/>
 * @see InjectUtils ;
 * <br>
 *
 *
 *
 *
 * @author S.Violet
 */

public abstract class TActivity extends Activity {

    private Logger mLogger;//日志打印器
    private CachedBitmapUtils mCachedBitmapUtils;//内置内存缓存的Bitmap工具

    private ActivitySettings settings;

    private int activityIndex;//对应在TApplication中的编号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        windowSetting();//窗口设置
        super.onCreate(savedInstanceState);
        InjectUtils.inject(this);

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
            ApplicationUtils.enableHardwareAccelerated(getWindow());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //回收掉内置Bitmap工具缓存的所有的位图
        if (mCachedBitmapUtils != null){
            mCachedBitmapUtils.recycleAll();
        }
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

    /**
     * 获得Activity内置的Bitmap工具, 带内存缓存/回收功能, 内存分配1/8应用内存
     * 且当Activity.onDestroy时会回收其所有的Bitmap
     *
     */
    public CachedBitmapUtils getCachedBitmapUtils(){
        if (mCachedBitmapUtils == null){
            //创建实例
            mCachedBitmapUtils = new CachedBitmapUtils(this);
            //设置日志打印器
            mCachedBitmapUtils.getBitmapCache().setLogger(getLogger());
        }
        return mCachedBitmapUtils;
    }

    /**
     * 获得Activity内置的Bitmap工具, 带内存缓存/回收功能,
     * 且当Activity.onDestroy时会回收其所有的Bitmap
     *
     * @param percent 缓存区占应用可用内存比例 (0, 0.5)
     */
    public CachedBitmapUtils getCachedBitmapUtils(float percent){
        if (mCachedBitmapUtils == null){
            //创建实例
            mCachedBitmapUtils = new CachedBitmapUtils(this, percent);
            //设置日志打印器
            mCachedBitmapUtils.getBitmapCache().setLogger(getLogger());
        }
        return mCachedBitmapUtils;
    }

}
