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
import android.support.annotation.RequiresApi;
import android.support.annotation.RequiresPermission;

import java.lang.ref.WeakReference;
import java.util.List;

import sviolet.turquoise.util.bluetooth.BluetoothUtils;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * Bluetooth Le Characteristic 连接器
 *
 * Created by S.Violet on 2017/8/15.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLECharacteristicConnector {

    public static final int ERROR_BLUETOOTH_UNSUPPORTED = -1;//设备不支持蓝牙
    public static final int ERROR_BLE_UNSUPPORTED = -2;//设备不支持BLE
    public static final int ERROR_BLUETOOTH_DISABLED = 1;//设备关闭了蓝牙
    public static final int ERROR_DEVICE_NOT_FOUND = 2;//找不到蓝牙设备
    public static final int ERROR_SERVICE_NOT_FOUND = 3;//蓝牙设备中的Service找不到(匹配不到serviceUUID)
    public static final int ERROR_CHARACTERISTIC_NOT_FOUND = 4;//蓝牙设备中的Characteristic找不到(匹配不到characteristicUUID)

    private TLogger logger = TLogger.get(this);

    private WeakReference<Context> contextWeakReference;

    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private String serviceUUID;
    private String characteristicUUID;
    private Callback callback;

    private boolean destroyed = false;

    /**
     * 尝试连接蓝牙设备
     * @param context context
     * @param deviceAddress 蓝牙设备地址
     * @param serviceUUID 服务UUID
     * @param characteristicUUID 特性UUID
     * @param callback 回调
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
        return new BLECharacteristicConnector(context, deviceAddress, serviceUUID, characteristicUUID, callback);
    }

    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    private BLECharacteristicConnector(@NonNull Context context, @NonNull String deviceAddress, @NonNull String serviceUUID, @NonNull String characteristicUUID, @NonNull Callback callback) {
        if (!BluetoothUtils.isBLESupported(context)) {
            destroy();
            callback.onError(ERROR_BLE_UNSUPPORTED);
            return;
        }
        BluetoothAdapter bluetoothAdapter = BluetoothUtils.getAdapter(context);
        if (bluetoothAdapter == null) {
            destroy();
            callback.onError(ERROR_BLUETOOTH_UNSUPPORTED);
            return;
        }
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON){
            destroy();
            callback.onError(ERROR_BLUETOOTH_DISABLED);
            return;
        }

        this.contextWeakReference = new WeakReference<>(context);
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.callback = callback;

        logger.d("ble-connect:start");
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device == null) {
            destroy();
            callback.onError(ERROR_DEVICE_NOT_FOUND);
            return;
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (destroyed){
                return;
            }
            logger.d("ble-connect:onConnectionStateChange(" + status + ", " + newState + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt.discoverServices();
                    logger.d("ble-connect:connected");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    destroy();
                    logger.d("ble-connect:disconnected");
                    callback.onDisconnected();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (destroyed){
                return;
            }
            logger.d("ble-connect:onServicesDiscovered(" + status + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logger.d("ble-connect:discovered");
                seekCharacteristic(gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (destroyed){
                return;
            }
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                logger.d("ble-connect:received data, length:" + data.length);
                callback.onReceive(data);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (destroyed){
                return;
            }
            byte[] data = characteristic.getValue();
            logger.d("ble-connect:received data, length:" + data.length);
            callback.onReceive(data);
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

    public void seekCharacteristic(List<BluetoothGattService> gattServices) {
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
                        callback.onReady();
                        return;
                    }
                }
                logger.d("ble-connect:characteristic not found");
                destroy();
                callback.onError(ERROR_CHARACTERISTIC_NOT_FOUND);
                return;
            }
        }
        logger.d("ble-connect:service not found");
        destroy();
        callback.onError(ERROR_SERVICE_NOT_FOUND);
    }

    /**
     * 向蓝牙设备传输数据, 必须在Callback.onReady后调用
     * @param data 数据
     * @return true:写入成功
     */
    public boolean writeData(byte[] data){
        if (destroyed){
            logger.d("Trying to write data through destroyed connector");
            return false;
        }
        if (data == null){
            logger.d("Trying to write null data");
            return false;
        }
        if (characteristic == null){
            logger.d("Trying to write data through unconnected connector");
            return false;
        }
        characteristic.setValue(data);
        bluetoothGatt.writeCharacteristic(characteristic);
        return true;
    }

    /**
     * [重要]销毁
     */
    public void destroy(){
        destroyed = true;
        if (bluetoothGatt != null){
            bluetoothGatt.close();
        }
    }

    private Context getContext() {
        return contextWeakReference.get();
    }

    public interface Callback {

        /**
         * 连接成功并获取到了characteristic, 可以开始读写数据
         */
        void onReady();

        /**
         * 连接已断开
         */
        void onDisconnected();

        /**
         * 接收到数据
         * @param data 接收到的数据
         */
        void onReceive(byte[] data);

        /**
         * 连接错误(之后连接会断开)
         * @param errorCode 错误码
         */
        void onError(int errorCode);

    }

}
