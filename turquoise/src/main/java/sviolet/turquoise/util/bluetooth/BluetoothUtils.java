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

package sviolet.turquoise.util.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

import java.util.ArrayList;
import java.util.List;

import sviolet.turquoise.enhance.common.WeakHandler;
import sviolet.turquoise.util.droid.DeviceUtils;
import sviolet.turquoise.util.droid.LocationUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * <p>蓝牙工具(状态判断/设备扫描)</p>
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothUtils {

    private static TLogger logger = TLogger.get(BluetoothUtils.class);

    /**
     * <p>判断设备是否支持BLE</p>
     * @param context context
     * @return true:支持BLE false:不支持BLE
     */
    public static boolean isBLESupported(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * <p>判断蓝牙是否开启(不包括开启中状态)</p>
     * @param context context
     * @return true:开启成功
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    public static boolean isBluetoothEnabled(@NonNull Context context){
        BluetoothAdapter adapter = getAdapter(context);
        return adapter != null && adapter.getState() == BluetoothAdapter.STATE_ON;
    }

    /**
     * <p>获取BluetoothAdapter</p>
     * @param context context
     * @return 返回空表示设备不支持蓝牙
     */
    @Nullable
    public static BluetoothAdapter getAdapter(@NonNull Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return null;
        }
        return bluetoothManager.getAdapter();
    }

    /**
     * <p>扫描BLE设备</p>
     *
     * <p>
     * 注意:<br/>
     * 1.需要声明权限:android.permission.BLUETOOTH_ADMIN/android.permission.BLUETOOTH/android.permission.ACCESS_FINE_LOCATION/android.permission.ACCESS_COARSE_LOCATION<br/>
     * 2.需要申请运行时权限:android.Manifest.permission.ACCESS_FINE_LOCATION/android.Manifest.permission.ACCESS_COARSE_LOCATION<br/>
     * 3.设备需要开启蓝牙<br/>
     * 4.安卓6.0以上设备需要开启位置信息<br/>
     * </p>
     *
     * <p>示例:</p>
     *
     * <pre>{@code
     *      //申请运行时权限(此为Turquoise库TActivity封装的方法)
     *      executePermissionTask(
     *          Manifest.permission.ACCESS_FINE_LOCATION,//位置信息权限
     *          null,//提示标题
     *          "扫描蓝牙设备需要开启位置信息并允许位置权限, 请通过权限申请",//提示信息
     *          new RuntimePermissionManager.RequestPermissionTask() {
     *
     *              //权限申请回调, 参数1:申请的权限列表, 参数2:结果列表 参数3:true表示所有权限均通过
     *              public void onResult(String[] permissions, int[] grantResults, boolean allGranted) {
     *
     *                  if (allGranted){
     *
     *                      //权限申请通过
     *                      //开始扫描BLE蓝牙设备
     *                      //参数1:当前Activity
     *                      //参数2:搜索30秒后停止
     *                      //参数3:建议true, 与Activity绑定生命周期(当Activity销毁时, 搜索自动停止)
     *                      //参数4:建议true, 验证设备状态(是否支持BLE/蓝牙是否开启/6.0以上定位是否开启)
     *                      BluetoothUtils.startBLEScan(this, 30000, true, true, new BluetoothUtils.ScanManager() {
     *
     *                          protected void onDeviceFound(List<BluetoothDevice> devices) {
     *                              //devices为当前搜索到的所有设备
     *                              //ScanManager内部维护了一个List, 记录了本次扫描到的所有设备
     *                          }
     *
     *                          protected void onCancel() {
     *                              //搜索取消/超时
     *                          }
     *
     *                          protected void onError(BluetoothUtils.ScanError errorCode) {
     *                              //搜索失败(终止)
     *                          }
     *
     *                      });
     *
     *                  } else {
     *
     *                      //权限申请未通过
     *                      //提醒用户没有权限无法扫描蓝牙设备
     *
     *                  }
     *
     *              }
     *
     *          });
     *
     * }</pre>
     *
     * @param activity        activity
     * @param timeout         超时时间, 单位millis, 设置0不超时
     * @param attachLifeCycle true:绑定生命周期(当activity销毁时, 会自动取消扫描), false:不绑定生命周期, 必须手动调用ScanManager.cancel()停止扫描
     * @param checkEnabled    true:验证设备是否支持BLE/蓝牙是否开启/6.0以上定位是否开启, false:不验证设备状态(需要自行验证)
     * @param scanManager     回调(并提供取消等管理操作), 回调并非UI线程
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"})
    public static void startBLEScan(@NonNull Activity activity, long timeout, boolean attachLifeCycle, boolean checkEnabled, @NonNull final ScanManager scanManager) {

        //获取Adapter
        BluetoothAdapter adapter = getAdapter(activity);
        if (adapter == null) {
            logger.d("bluetooth-scan:error_bluetooth_unsupported");
            scanManager.onError(ScanError.ERROR_BLUETOOTH_UNSUPPORTED);
            return;
        }

        if (checkEnabled) {

            //判断是否支持BLE
            if (!isBLESupported(activity)) {
                logger.d("bluetooth-scan:error_ble_unsupported");
                scanManager.onError(ScanError.ERROR_BLE_UNSUPPORTED);
                return;
            }

            //判断蓝牙是否开启
            if (adapter.getState() != BluetoothAdapter.STATE_ON) {
                logger.d("bluetooth-scan:error_bluetooth_disabled");
                scanManager.onError(ScanError.ERROR_BLUETOOTH_DISABLED);
                return;
            }

            //安卓6.0以上扫描蓝牙需要开启位置信息,
            if (DeviceUtils.getVersionSDK() >= 23) {
                //判断位置信息是否开启
                if (!LocationUtils.isEnabled(activity)) {
                    logger.d("bluetooth-scan:error_location_disabled");
                    scanManager.onError(ScanError.ERROR_LOCATION_DISABLED);
                    return;
                }
            }

        }

        //回调
        BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
            @Override
            @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH", "android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"})
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (scanManager.devices.contains(device)) {
                    return;
                }
                logger.d("bluetooth-scan:device found:<" + device.getName() + "><" + device.getAddress() + ">");
                if (scanManager.filter(device, rssi, scanRecord)) {
                    logger.d("bluetooth-scan:valid device");
                    scanManager.addDevice(device);
                    scanManager.onDeviceFound(scanManager.devices);
                }
            }
        };

        //开始监听蓝牙扫描
        scanManager.setAdapter(adapter);
        scanManager.setCallback(callback);
        adapter.startLeScan(callback);
        logger.d("bluetooth-scan:start");

        //绑定生命周期
        if (attachLifeCycle) {
            LifeCycleUtils.attach(activity, scanManager);
        }

        //时间限制
        if (timeout > 0) {
            new CancelHandler(scanManager).sendEmptyMessageDelayed(0, timeout);
        }

    }

    /**
     * <p>蓝牙扫描回调(并提供取消等管理操作), 回调并非UI线程</p>
     *
     * <p>当attachLifeCycle==true时, 与Activity生命周期绑定, 在Activity.onDestroy时, 自动取消扫描</p>
     */
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static abstract class ScanManager implements LifeCycle {

        private BluetoothAdapter adapter;
        private BluetoothAdapter.LeScanCallback callback;
        private List<BluetoothDevice> devices = new ArrayList<>();
        private boolean canceled = false;

        /**
         * 当扫描到新设备时回调该方法.
         * ScanManager内部维护了一个List, 记录了本次扫描到的所有设备.
         *
         * @param devices 从调用BluetoothUtils.startBLEScan()开始, 扫描到的所有设备(累加)
         */
        protected abstract void onDeviceFound(List<BluetoothDevice> devices);

        /**
         * 当扫描被取消/超时/activity销毁时回调
         */
        protected abstract void onCancel();

        /**
         * 当扫描出错时回调(注意此时扫描已终止)
         *
         * @param error 错误类型
         */
        protected abstract void onError(ScanError error);

        /**
         * 可以复写该方法实现设备过滤, 默认全通过
         *
         * @param device     设备信息
         * @param rssi       信号强度
         * @param scanRecord scan record
         * @return true:记录设备 false:忽略设备
         */
        protected boolean filter(BluetoothDevice device, int rssi, byte[] scanRecord) {
            return true;
        }

        /**
         * <p>[重要]取消扫描</p>
         *
         * <p>若attachLifeCycle==false时, 请在Context结束时调用该方法取消扫描</p>
         */
        public final void cancel() {
            if (!canceled && adapter != null && callback != null) {
                adapter.stopLeScan(callback);
                onCancel();
                logger.d("bluetooth-scan:canceled");
            }
            canceled = true;
        }

        private void setAdapter(BluetoothAdapter adapter) {
            this.adapter = adapter;
        }

        private void setCallback(BluetoothAdapter.LeScanCallback callback) {
            this.callback = callback;
        }

        private void addDevice(BluetoothDevice device) {
            if (device != null && devices != null) {
                devices.add(device);
            }
        }

        @Override
        public final void onCreate() {
        }

        @Override
        public final void onStart() {
        }

        @Override
        public final void onResume() {
        }

        @Override
        public final void onPause() {
        }

        @Override
        public final void onStop() {
        }

        @Override
        public final void onDestroy() {
            cancel();
            adapter = null;
            callback = null;
            devices = null;
        }
    }

    /**
     * 用于回调主线程
     */
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class CancelHandler extends WeakHandler<ScanManager> {

        private CancelHandler(ScanManager host) {
            super(Looper.getMainLooper(), host);
        }

        @Override
        protected void handleMessageWithHost(Message msg, ScanManager host) {
            host.cancel();
        }

    }

    /**
     * 扫描错误
     */
    public enum ScanError {
        /**
         * 设备不支持蓝牙
         */
        ERROR_BLUETOOTH_UNSUPPORTED,
        /**
         * 设备不支持BLE
         */
        ERROR_BLE_UNSUPPORTED,
        /**
         * 设备关闭了蓝牙
         */
        ERROR_BLUETOOTH_DISABLED,
        /**
         * 设备关闭了位置信息
         */
        ERROR_LOCATION_DISABLED
    }

}
