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
 * <p>
 * Bluetooth Le Characteristic connectorbr/>
 * BLE特性连接器.<br/>
 * 与一个蓝牙设备的一个特性连接, 发送或接受数据, 维护连接关系.<br/>
 * </p>
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLECharacteristicConnector implements LifeCycle {

    private TLogger logger = TLogger.get(this);

    private WeakReference<Context> contextWeakReference;//context弱引用

    private BluetoothGatt bluetoothGatt;//协议实例
    private BluetoothGattCharacteristic characteristic;//特性实例
    private String deviceName;//设备名称
    private String deviceAddress;//设备MAC地址
    private String serviceUUID;//服务UUID(取决于对方蓝牙设备)
    private String characteristicUUID;//特性UUID(取决于对方蓝牙设备)
    private Callback callback;//回调

    private Status connectStatus;//连接状态

    private ReentrantLock writeLock = new ReentrantLock();

    /**
     * <p>尝试连接蓝牙设备</p>
     *
     * <p>示例</p>
     *
     * <pre>{@code
     *      BLECharacteristicConnector.connect(
     *          this,//Activity
     *          "50:8C:B1:69:83:1A",//蓝牙设备地址
     *          "0000ffe0-0000-1000-8000-00805f9b34fb",//要连接的服务UUID
     *          "0000ffe1-0000-1000-8000-00805f9b34fb",//要连接的特性UUID
     *          true,//true:绑定生命周期, 当activity销毁时, 会自动断开连接 false:不绑定生命周期, 必须手动调用BLECharacteristicConnector.destroy()销毁连接
     *          new BLECharacteristicConnector.Callback() {
     *
     *              public void onReady(BLECharacteristicConnector connector) {
     *                  //连接成功, 可以开始读写数据
     *              }
     *
     *              public void onDisconnected(BLECharacteristicConnector connector) {
     *                  //连接断开
     *                  //可以在此处进行重连BLECharacteristicConnector.reconnect()或BLECharacteristicConnector.connect(...)
     *              }
     *
     *              public void onReceiveSucceed(BLECharacteristicConnector connector, byte[] data) {
     *                  //收到有效数据
     *              }
     *
     *              public void onReceiveFailed(BLECharacteristicConnector connector, int status) {
     *                  //读取数据失败
     *                  //一般不处理该事件
     *              }
     *
     *              public void onError(BLECharacteristicConnector connector, BLECharacteristicConnector.Error errorCode, Throwable throwable) {
     *                  //连接错误
     *                  //注意, 此时连接已断开
     *                  //可以在此处进行重连BLECharacteristicConnector.reconnect()或BLECharacteristicConnector.connect(...)
     *              }
     *
     *          });
     * }</pre>
     *
     * @param context            activity
     * @param deviceAddress      蓝牙设备地址
     * @param serviceUUID        指定服务UUID
     * @param characteristicUUID 特性UUID
     * @param attachLifeCycle    true:绑定生命周期, 当activity销毁时, 会自动断开连接 false:不绑定生命周期, 必须手动调用BLECharacteristicConnector.destroy()销毁连接
     * @param callback           回调, 非UI线程
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
     * <p>尝试连接蓝牙设备, 不绑定生命周期, 必须手动调用BLECharacteristicConnector.destroy()销毁连接</p>
     *
     * <p>示例</p>
     *
     * <pre>{@code
     *      BLECharacteristicConnector.connect(
     *          getApplicationContext(),//Context
     *          "50:8C:B1:69:83:1A",//蓝牙设备地址
     *          "0000ffe0-0000-1000-8000-00805f9b34fb",//要连接的服务UUID
     *          "0000ffe1-0000-1000-8000-00805f9b34fb",//要连接的特性UUID
     *          new BLECharacteristicConnector.Callback() {
     *
     *              public void onReady(BLECharacteristicConnector connector) {
     *                  //连接成功, 可以开始读写数据
     *              }
     *
     *              public void onDisconnected(BLECharacteristicConnector connector) {
     *                  //连接断开
     *                  //可以在此处进行重连BLECharacteristicConnector.reconnect()或BLECharacteristicConnector.connect(...)
     *              }
     *
     *              public void onReceiveSucceed(BLECharacteristicConnector connector, byte[] data) {
     *                  //收到有效数据
     *              }
     *
     *              public void onReceiveFailed(BLECharacteristicConnector connector, int status) {
     *                  //读取数据失败
     *                  //一般不处理该事件
     *              }
     *
     *              public void onError(BLECharacteristicConnector connector, BLECharacteristicConnector.Error errorCode, Throwable throwable) {
     *                  //连接错误
     *                  //注意, 此时连接已断开
     *                  //可以在此处进行重连BLECharacteristicConnector.reconnect()或BLECharacteristicConnector.connect(...)
     *              }
     *
     *          });
     * }</pre>
     *
     * @param context            activity
     * @param deviceAddress      蓝牙设备地址
     * @param serviceUUID        指定服务UUID
     * @param characteristicUUID 特性UUID
     * @param callback           回调, 非UI线程
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
        //连接
        connect();
    }

    /**
     * 开始连接
     */
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

    /**
     * 开始连接
     */
    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    private void connect0() {
        try {
            //检查Context
            Context context = contextWeakReference.get();
            if (context == null) {
                disconnect();
                callback.onError(this, Error.ERROR_EXCEPTION, new Exception("ApplicationContext destroyed can not connect"));
                return;
            }
            //检查是否支持BLE
            if (!BluetoothUtils.isBLESupported(context)) {
                disconnect();
                callback.onError(this, Error.ERROR_BLE_UNSUPPORTED, null);
                return;
            }
            //获取Adapter
            BluetoothAdapter bluetoothAdapter = BluetoothUtils.getAdapter(context);
            if (bluetoothAdapter == null) {
                disconnect();
                callback.onError(this, Error.ERROR_BLUETOOTH_UNSUPPORTED, null);
                return;
            }
            //判断蓝牙是否开启
            if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON) {
                disconnect();
                callback.onError(this, Error.ERROR_BLUETOOTH_DISABLED, null);
                return;
            }

            //连接
            logger.d("ble-connect:start, address:" + deviceAddress + ", serviceUUID:" + serviceUUID + ", characteristicUUID:" + characteristicUUID);
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            if (device == null) {
                disconnect();
                callback.onError(this, Error.ERROR_DEVICE_NOT_FOUND, null);
                return;
            }

            deviceName = device.getName();
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

    /**
     * 回调
     */
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (connectStatus == Status.DISCONNECTED || connectStatus == Status.DESTROYED){
                return;
            }
            try {
                logger.d("ble-connect:onConnectionStateChange(" + status + ", " + newState + ")");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    //通讯成功
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        //连接成功
                        bluetoothGatt.discoverServices();
                        logger.d("ble-connect:connected");
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        //连接断开
                        disconnect();
                        logger.d("ble-connect:disconnected");
                        callback.onDisconnected(BLECharacteristicConnector.this);
                    }
                } else {
                    //通讯失败
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
                    //找到设备提供的服务
                    logger.d("ble-connect:discovered");
                    seekCharacteristic(gatt.getServices());
                } else {
                    //通讯失败
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
                    //成功读取数据
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
                //数据变化
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

    /**
     * 查找特性
     */
    private void seekCharacteristic(List<BluetoothGattService> gattServices) {
        //从蓝牙设备提供的服务列表中找到需要的服务
        logger.d("ble-connect:discovered " + gattServices.size() + " services");
        logger.d("ble-connect:finding service with uuid:" + serviceUUID);
        for (BluetoothGattService gattService : gattServices) {
            logger.d("ble-connect:checking:" + gattService.getUuid());
            if (gattService.getUuid().toString().equalsIgnoreCase(serviceUUID)) {
                logger.d("ble-connect:service found");
                //从蓝牙设备提供服务中找到需要的特性
                List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
                logger.d("ble-connect:discovered " + gattCharacteristics.size() + " characteristics");
                logger.d("ble-connect:finding characteristic with uuid:" + characteristicUUID);
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    logger.d("ble-connect:checking:" + gattCharacteristic.getUuid());
                    if (gattCharacteristic.getUuid().toString().equalsIgnoreCase(characteristicUUID)) {
                        logger.d("ble-connect:characteristic found");
                        this.characteristic = gattCharacteristic;
                        bluetoothGatt.setCharacteristicNotification(characteristic, true);
                        if (connectStatus != Status.DESTROYED){
                            connectStatus = Status.READY;
                            callback.onReady(this);
                        }
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
     * <p>向蓝牙设备传输数据</p>
     *
     * <p>
     * 注意:<br/>
     * 1.必须在Callback.onReady()后调用.<br/>
     * 2.该方法有同步锁, 会阻塞线程, 请设置合适的超时时间. 超过时间会退出竞争, 并返回失败.<br/>
     * </p>
     *
     * @param data 数据
     * @param timeout 写入超时时间(等待时线程阻塞), 单位millis, 必须>0
     * @return true:写入成功 false:写入超时
     */
    public boolean writeData(byte[] data, int timeout){
        if (data == null){
            logger.d("ERROR:Trying to write null data");
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
                logger.e("ERROR:Trying to write data through disconnected connector, please reconnect or new one");
                return false;
            }
            if (connectStatus == Status.CONNECTING) {
                logger.e("ERROR:Trying to write data through connecting connector, please wait for ready");
                return false;
            }
            if (characteristic == null){
                logger.e("ERROR:Trying to write data through unconnected connector, please wait for ready");
                return false;
            }
            //写数据
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
     * <p>重新连接. 注意销毁后无法重连.</p>
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
     * <p>断开连接, 可以重连</p>
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
            callback.onDisconnected(this);
        }
    }

    /**
     * 销毁, 无法重连
     */
    public void destroy(){
        onDestroy();
    }

    /**
     * @return 获得当前连接状态
     */
    public Status getConnectStatus(){
        return connectStatus;
    }

    /**
     * @return 获得连接的设备地址(断开后仍会返回)
     */
    @Nullable
    public String getDeviceAddress(){
        return deviceAddress;
    }

    /**
     * @return 获得连接的设备名称(断开后仍会返回)
     */
    @Nullable
    public String getDeviceName(){
        return deviceName;
    }

    private Context getContext() {
        return contextWeakReference.get();
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
        connectStatus = Status.DESTROYED;
        disconnect();
    }

    /**
     * 回调, 非UI线程
     */
    public interface Callback {

        /**
         * 连接成功, 可以开始读写数据
         */
        void onReady(BLECharacteristicConnector connector);

        /**
         * 连接已断开, 尝试重新连接, 或放弃使用
         */
        void onDisconnected(BLECharacteristicConnector connector);

        /**
         * 接收到正常的数据
         * @param data 接收到的数据
         */
        void onReceiveSucceed(BLECharacteristicConnector connector, byte[] data);

        /**
         * 接受数据错误
         * @param status 接收到的错误状态(BluetoothGatt.GATT_???)
         */
        void onReceiveFailed(BLECharacteristicConnector connector, int status);

        /**
         * 连接错误. 注意:此时连接已断开.
         * @param error 错误码
         * @param throwable 异常(可能为空)
         */
        void onError(BLECharacteristicConnector connector, Error error, @Nullable Throwable throwable);

    }

    /**
     * 错误
     */
    public enum Error {
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
         * 找不到蓝牙设备
         */
        ERROR_DEVICE_NOT_FOUND,
        /**
         * 无法识别的蓝牙设备, 蓝牙设备中匹配不到指定的serviceUUID
         */
        ERROR_SERVICE_NOT_FOUND,
        /**
         * 无法识别的蓝牙设备, 蓝牙设备中匹配不到指定的characteristicUUID
         */
        ERROR_CHARACTERISTIC_NOT_FOUND,
        /**
         * 蓝牙连接失败(附带Exception)
         */
        ERROR_CONNECT_FAILED,
        /**
         * 蓝牙连接失败(查找服务)(附带Exception)
         */
        ERROR_DISCOVER_FAILED,
        /**
         * 其他异常(附带Exception)
         */
        ERROR_EXCEPTION
    }

    /**
     * 连接状态
     */
    public enum Status {
        /**
         * 连接中,暂不可用
         */
        CONNECTING,
        /**
         * 连接可用, 可进行读写操作
         */
        READY,
        /**
         * 连接已断开, 允许重连
         */
        DISCONNECTED,
        /**
         * 已销毁, 不允许重连
         */
        DESTROYED
    }

}
