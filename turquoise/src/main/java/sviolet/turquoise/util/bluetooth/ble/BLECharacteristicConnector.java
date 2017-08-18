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

package sviolet.turquoise.util.bluetooth.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import sviolet.turquoise.util.bluetooth.BluetoothUtils;
import sviolet.turquoise.utilx.lifecycle.LifeCycle;
import sviolet.turquoise.utilx.lifecycle.LifeCycleUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * Bluetooth Le Characteristic 连接器
 *
 * Created by S.Violet on 2017/8/15.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLECharacteristicConnector implements LifeCycle {

    private TLogger logger = TLogger.get(this);

    private WeakReference<Context> contextWeakReference;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private String deviceAddress;
    private String serviceUUID;
    private String characteristicUUID;
    private Callback callback;

    private Status connectStatus;

    private ReentrantLock writeLock = new ReentrantLock();

    /**
     * 尝试连接蓝牙设备
     *
     * @param context            activity
     * @param deviceAddress      蓝牙设备地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特性UUID
     * @param attachLifeCycle    true:绑定生命周期(当activity销毁时, 会自动断开连接) false:不绑定生命周期(必须手动调用disconnect断开连接)
     * @param callback           回调
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    public static BLECharacteristicConnector connect(@NonNull Activity context, @NonNull String deviceAddress, @NonNull String serviceUUID, @NonNull String characteristicUUID, boolean attachLifeCycle, @NonNull Callback callback) {
        BLECharacteristicConnector connector = connect(context, deviceAddress, serviceUUID, characteristicUUID, callback);
        if (attachLifeCycle){
            LifeCycleUtils.attach(context, connector);
        }
        return connector;
    }

    /**
     * 尝试连接蓝牙设备(不绑定生命周期(必须手动调用disconnect断开连接))
     *
     * @param context            context
     * @param deviceAddress      蓝牙设备地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特性UUID
     * @param callback           回调
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    public static BLECharacteristicConnector connect(@NonNull Context context, @NonNull String deviceAddress, @NonNull String serviceUUID, @NonNull String characteristicUUID, @NonNull Callback callback) {
        if (context == null) {
            throw new IllegalArgumentException("context is null");
        }
        if (deviceAddress == null) {
            throw new IllegalArgumentException("address is null");
        }
        if (serviceUUID == null) {
            throw new IllegalArgumentException("serviceUUID is null");
        }
        if (characteristicUUID == null) {
            throw new IllegalArgumentException("characteristicUUID is null");
        }
        return new BLECharacteristicConnector(context.getApplicationContext(), deviceAddress, serviceUUID, characteristicUUID, callback);
    }

    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    private BLECharacteristicConnector(@NonNull Context context, @NonNull String deviceAddress, @NonNull String serviceUUID, @NonNull String characteristicUUID, @NonNull Callback callback) {
        this.contextWeakReference = new WeakReference<>(context);
        this.deviceAddress = deviceAddress;
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.callback = callback;
        connect();
    }

    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    private void connect(){
        new Thread(new Runnable() {
            @Override
            @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
            public void run() {
                connect0();
            }
        }).start();
    }

    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    private void connect0() {
        try {
            Context context = contextWeakReference.get();
            if (context == null) {
                disconnect();
                callback.onError(this, Error.ERROR_EXCEPTION, new Exception("ApplicationContext destroyed can not connect"));
                return;
            }
            if (!BluetoothUtils.isBLESupported(context)) {
                disconnect();
                callback.onError(this, Error.ERROR_BLE_UNSUPPORTED, null);
                return;
            }
            BluetoothAdapter bluetoothAdapter = BluetoothUtils.getAdapter(context);
            if (bluetoothAdapter == null) {
                disconnect();
                callback.onError(this, Error.ERROR_BLUETOOTH_UNSUPPORTED, null);
                return;
            }
            if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
                disconnect();
                callback.onError(this, Error.ERROR_BLUETOOTH_DISABLED, null);
                return;
            }

            logger.d("ble-connect:start, address:" + deviceAddress + ", serviceUUID:" + serviceUUID + ", characteristicUUID:" + characteristicUUID);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device == null) {
                disconnect();
                callback.onError(this, Error.ERROR_DEVICE_NOT_FOUND, null);
                return;
            }

            bluetoothGatt = device.connectGatt(context, false, gattCallback);

            if (connectStatus != Status.DESTROYED){
                connectStatus = Status.CONNECTING;
            }
        } catch (Throwable t){
            if (connectStatus == Status.CONNECTING || connectStatus == Status.READY){
                disconnect();
                callback.onError(this, Error.ERROR_EXCEPTION, t);
            }
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (connectStatus == Status.DISCONNECTED || connectStatus == Status.DESTROYED){
                return;
            }
            try {
                logger.d("ble-connect:onConnectionStateChange(" + status + ", " + newState + ")");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        bluetoothGatt.discoverServices();
                        logger.d("ble-connect:connected");
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        disconnect();
                        logger.d("ble-connect:disconnected");
                        callback.onDisconnected(BLECharacteristicConnector.this);
                    }
                } else {
                    disconnect();
                    logger.d("ble-connect:connect failed with status(BlueToothGatt.GATT_???):" + status);
                    callback.onError(BLECharacteristicConnector.this, Error.ERROR_CONNECT_FAILED, new Exception("ble-connect:connect failed with status(BlueToothGatt.GATT_???):" + status));
                }
            } catch (Throwable t){
                if (connectStatus == Status.CONNECTING || connectStatus == Status.READY){
                    disconnect();
                    callback.onError(BLECharacteristicConnector.this, Error.ERROR_EXCEPTION, t);
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (connectStatus == Status.DISCONNECTED || connectStatus == Status.DESTROYED){
                return;
            }
            try {
                logger.d("ble-connect:onServicesDiscovered(" + status + ")");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    logger.d("ble-connect:discovered");
                    seekCharacteristic(gatt.getServices());
                } else {
                    disconnect();
                    logger.d("ble-connect:discover failed with status(BlueToothGatt.GATT_???):" + status);
                    callback.onError(BLECharacteristicConnector.this, Error.ERROR_DISCOVER_FAILED, new Exception("ble-connect:discover failed with status(BlueToothGatt.GATT_???):" + status));
                }
            } catch (Throwable t){
                if (connectStatus == Status.CONNECTING || connectStatus == Status.READY){
                    disconnect();
                    callback.onError(BLECharacteristicConnector.this, Error.ERROR_EXCEPTION, t);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (connectStatus == Status.DISCONNECTED || connectStatus == Status.DESTROYED){
                return;
            }
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    byte[] data = characteristic.getValue();
                    logger.d("ble-connect:received data, length:" + data.length);
                    callback.onReceiveSucceed(BLECharacteristicConnector.this, data);
                } else {
                    logger.e("ble-connect:received error, status(BluetoothGatt.GATT_???):" + status);
                    callback.onReceiveFailed(BLECharacteristicConnector.this, status);
                }
            } catch (Throwable t){
                if (connectStatus == Status.CONNECTING || connectStatus == Status.READY){
                    disconnect();
                    callback.onError(BLECharacteristicConnector.this, Error.ERROR_EXCEPTION, t);
                }
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (connectStatus == Status.DISCONNECTED || connectStatus == Status.DESTROYED){
                return;
            }
            try {
                byte[] data = characteristic.getValue();
                logger.d("ble-connect:received data, length:" + data.length);
                callback.onReceiveSucceed(BLECharacteristicConnector.this, data);
            } catch (Throwable t){
                if (connectStatus == Status.CONNECTING || connectStatus == Status.READY){
                    disconnect();
                    callback.onError(BLECharacteristicConnector.this, Error.ERROR_EXCEPTION, t);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor bd, int status) {
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int a, int b) {
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int a) {
        }

    };

    private void seekCharacteristic(List<BluetoothGattService> gattServices) {
        logger.d("ble-connect:discovered " + gattServices.size() + " services");
        logger.d("ble-connect:finding service with uuid:" + serviceUUID);
        for (BluetoothGattService gattService : gattServices) {
            logger.d("ble-connect:checking:" + gattService.getUuid());
            if (gattService.getUuid().toString().equalsIgnoreCase(serviceUUID)) {
                logger.d("ble-connect:service found");
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                logger.d("ble-connect:discovered " + gattCharacteristics.size() + " characteristics");
                logger.d("ble-connect:finding characteristic with uuid:" + characteristicUUID);
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    logger.d("ble-connect:checking:" + gattCharacteristic.getUuid());
                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(characteristicUUID)) {
                        logger.d("ble-connect:characteristic found");
                        this.characteristic = gattCharacteristic;
                        bluetoothGatt.setCharacteristicNotification(characteristic, true);
                        callback.onReady(this);
                        return;
                    }
                }
                logger.d("ble-connect:characteristic not found");
                disconnect();
                callback.onError(this, Error.ERROR_CHARACTERISTIC_NOT_FOUND, null);
                return;
            }
        }
        logger.d("ble-connect:service not found");
        disconnect();
        callback.onError(this, Error.ERROR_SERVICE_NOT_FOUND, null);
    }

    /**
     * 向蓝牙设备传输数据, 必须在Callback.onReady后调用, 同步锁操作, 请设置合适的等待时间, 超过时间退出锁竞争, 并返回失败
     * @param data 数据
     * @param timeout 写入排队等待时间, millis, 必须>0
     * @return true:写入成功
     */
    public boolean writeData(byte[] data, int timeout){
        if (data == null){
            logger.d("Trying to write null data");
            return false;
        }
        if (timeout <= 0){
            throw new IllegalArgumentException("timeout must > 0");
        }
        //如果超时则不需要进行unlock操作
        try {
            if (!writeLock.tryLock(timeout, TimeUnit.MILLISECONDS)){
                return false;
            }
        } catch (InterruptedException e) {
            return false;
        }
        try {
            if (connectStatus == Status.DISCONNECTED || connectStatus == Status.DESTROYED){
                logger.d("Trying to write data through disconnected connector");
                return false;
            }
            if (characteristic == null){
                logger.d("Trying to write data through unconnected connector");
                return false;
            }
            characteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(characteristic);
            return true;
        } catch (Throwable t){
            if (connectStatus == Status.CONNECTING || connectStatus == Status.READY){
                disconnect();
                callback.onError(this, Error.ERROR_EXCEPTION, t);
            }
        } finally {
            writeLock.unlock();
        }
        return false;
    }

    /**
     * 重新连接
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    public boolean reconnect(){
        if (connectStatus == Status.DISCONNECTED){
            connect();
            return true;
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public void disconnect(){
        if (connectStatus != Status.DESTROYED){
            connectStatus = Status.DISCONNECTED;
        }
        if (bluetoothGatt != null){
            try {
                bluetoothGatt.close();
            } catch (Throwable ignore){
            }
            bluetoothGatt = null;
            characteristic = null;
        }
    }

    /**
     * 销毁实例(无法再重连)
     */
    public void destroy(){
        onDestroy();
    }

    public Status getConnectStatus(){
        return connectStatus;
    }

    public String getDeviceAddress(){
        return deviceAddress;
    }

    private Context getContext() {
        return contextWeakReference.get();
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onStop() {
    }

    @Override
    public void onDestroy() {
        connectStatus = Status.DESTROYED;
        disconnect();
    }

    public interface Callback {

        /**
         * 连接成功并获取到了characteristic, 可以开始读写数据
         */
        void onReady(BLECharacteristicConnector connector);

        /**
         * 连接已断开, 尝试重新连接, 或放弃使用
         */
        void onDisconnected(BLECharacteristicConnector connector);

        /**
         * 接收到正常数据
         * @param data 接收到的数据
         */
        void onReceiveSucceed(BLECharacteristicConnector connector, byte[] data);

        /**
         * 接收到异常数据
         * @param status 接收到的错误状态(BluetoothGatt.GATT_???)
         */
        void onReceiveFailed(BLECharacteristicConnector connector, int status);

        /**
         * 连接错误(且连接已断开)
         * @param error 错误码(BLECharacteristicConnector.ERROR)
         * @param throwable 异常(可能为空)
         */
        void onError(BLECharacteristicConnector connector, Error error, @Nullable Throwable throwable);

    }

    public enum Error {
        ERROR_BLUETOOTH_UNSUPPORTED,//设备不支持蓝牙
        ERROR_BLE_UNSUPPORTED,//设备不支持BLE
        ERROR_BLUETOOTH_DISABLED,//设备关闭了蓝牙
        ERROR_DEVICE_NOT_FOUND,//找不到蓝牙设备
        ERROR_SERVICE_NOT_FOUND,//蓝牙设备中的Service找不到(匹配不到serviceUUID)
        ERROR_CHARACTERISTIC_NOT_FOUND,//蓝牙设备中的Characteristic找不到(匹配不到characteristicUUID)
        ERROR_CONNECT_FAILED,//蓝牙连接失败(附带Exception)
        ERROR_DISCOVER_FAILED,//蓝牙连接(查找服务)失败(附带Exception)
        ERROR_EXCEPTION//其他异常(附带Exception)
    }

    public enum Status {
        CONNECTING,//连接中,暂不可用
        READY,//连接可用
        DISCONNECTED,//连接已断开
        DESTROYED//已销毁
    }

}
