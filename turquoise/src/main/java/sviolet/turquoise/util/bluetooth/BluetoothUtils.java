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
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * 蓝牙扫描工具
 *
 * Created by S.Violet on 2017/8/15.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothUtils {

    public static final int ERROR_BLUETOOTH_UNSUPPORTED = -1;//设备不支持蓝牙
    public static final int ERROR_BLE_UNSUPPORTED = -2;//设备不支持BLE
    public static final int ERROR_BLUETOOTH_DISABLED = 1;//设备关闭了蓝牙

    private static TLogger logger = TLogger.get(BluetoothUtils.class);

    /**
     * 判断设备是否支持BLE
     */
    public static boolean isBLESupported(@NonNull Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 获取BluetoothAdapter
     * @return 返回空表示设备不支持蓝牙
     */
    @Nullable
    public static BluetoothAdapter getAdapter(@NonNull Context context) {
        final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null){
            return null;
        }
        return bluetoothManager.getAdapter();
    }

    /**
     * 开始扫描BLE设备
     * @param activity activity, 当activity销毁时, 会自动取消扫描
     * @param timeout 超时时间, 0不超时
     * @param scanManager 回调及管理
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    public static void startBLEScan(@NonNull Activity activity, long timeout, @NonNull final ScanManager scanManager) {
        if (!isBLESupported(activity)){
            logger.d("bluetooth-scan:error_ble_unsupported");
            scanManager.onError(ERROR_BLE_UNSUPPORTED);
            return;
        }

        BluetoothAdapter adapter = getAdapter(activity);
        if (adapter == null){
            logger.d("bluetooth-scan:error_bluetooth_unsupported");
            scanManager.onError(ERROR_BLUETOOTH_UNSUPPORTED);
            return;
        }

        if (adapter.getState() != BluetoothAdapter.STATE_ON){
            logger.d("bluetooth-scan:error_bluetooth_disabled");
            scanManager.onError(ERROR_BLUETOOTH_DISABLED);
            return;
        }

        BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (scanManager.devices.contains(device)){
                    return;
                }
                logger.d("bluetooth-scan:device found:" + device);
                if (scanManager.filter(device, rssi, scanRecord)) {
                    logger.d("bluetooth-scan:valid device");
                    scanManager.addDevice(device);
                    scanManager.onDeviceFound(scanManager.devices);
                }
            }
        };

        scanManager.setAdapter(adapter);
        scanManager.setCallback(callback);
        adapter.startLeScan(callback);
        logger.d("bluetooth-scan:start");
        LifeCycleUtils.attach(activity, scanManager);

        if (timeout > 0){
            new CancelHandler(scanManager).sendEmptyMessageDelayed(0, timeout);
        }

    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static abstract class ScanManager implements LifeCycle {

        private BluetoothAdapter adapter;
        private BluetoothAdapter.LeScanCallback callback;
        private List<BluetoothDevice> devices = new ArrayList<>();
        private boolean canceled = false;

        /**
         * 当扫描到设备时回调该方法, 内部维护了一个设备列表, 保存所有已扫描到的设备
         * @param devices
         */
        protected abstract void onDeviceFound(List<BluetoothDevice> devices);

        /**
         * 当扫描被手动取消/超时/activity销毁时调用
         */
        protected abstract void onCancel();

        /**
         * 当扫描出错时调用
         */
        protected abstract void onError(int errorCode);

        /**
         * 复写该方法实现设备过滤
         * @param device 设备信息
         * @param rssi 信号强度
         * @param scanRecord scan record
         * @return true:记录设备 false:不记录设备
         */
        protected boolean filter(BluetoothDevice device, int rssi, byte[] scanRecord){
            return true;
        }

        /**
         * 取消扫描
         */
        public final void cancel(){
            if (!canceled && adapter != null && callback != null){
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

        private void addDevice(BluetoothDevice device){
            if (device != null && devices != null)
                devices.add(device);
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

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private static class CancelHandler extends WeakHandler<ScanManager> {
        public CancelHandler(ScanManager host) {
            super(Looper.getMainLooper(), host);
        }
        @Override
        protected void handleMessageWithHost(Message msg, ScanManager host) {
            host.cancel();
        }
    }

}
