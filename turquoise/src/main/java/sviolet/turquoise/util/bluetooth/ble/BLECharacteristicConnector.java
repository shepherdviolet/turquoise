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
import sviolet.turquoise.util.bluetooth.exception.BLEUnsupportedException;
import sviolet.turquoise.util.bluetooth.exception.BluetoothDisabledException;
import sviolet.turquoise.util.bluetooth.exception.BluetoothUnsupportedException;
import sviolet.turquoise.util.bluetooth.exception.DeviceNotFoundException;
import sviolet.turquoise.utilx.tlogger.TLogger;

/**
 * Bluetooth Le Characteristic 连接器
 *
 * Created by S.Violet on 2017/8/15.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLECharacteristicConnector {

    private TLogger logger = TLogger.get(this);

    private WeakReference<Context> contextWeakReference;

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;
    private BluetoothGattCharacteristic characteristic;
    private String serviceUUID;
    private String characteristicUUID;
    private Callback callback;

    @RequiresPermission(allOf = {"android.permission.BLUETOOTH_ADMIN", "android.permission.BLUETOOTH"})
    public static BLECharacteristicConnector connect(@NonNull Context context, @NonNull String deviceAddress, @NonNull String serviceUUID, @NonNull String characteristicUUID, @NonNull Callback callback) throws BLEUnsupportedException, BluetoothUnsupportedException, BluetoothDisabledException, DeviceNotFoundException {
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
    private BLECharacteristicConnector(@NonNull Context context, @NonNull String deviceAddress, @NonNull String serviceUUID, @NonNull String characteristicUUID, @NonNull Callback callback) throws BLEUnsupportedException, BluetoothUnsupportedException, BluetoothDisabledException, DeviceNotFoundException {
        if (!BluetoothUtils.isBLESupported(context)) {
            throw new BLEUnsupportedException("BLE unsupported in this device");
        }
        bluetoothAdapter = BluetoothUtils.getAdapter(context);
        if (bluetoothAdapter == null) {
            throw new BluetoothUnsupportedException("Bluetooth unsupported in this device");
        }
        if (bluetoothAdapter.getState() != BluetoothAdapter.STATE_ON){
            throw new BluetoothDisabledException("Bluetooth disabled in system settings");
        }

        this.contextWeakReference = new WeakReference<>(context);
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.callback = callback;

        logger.d("ble-connect:start");
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device == null) {
            throw new DeviceNotFoundException("Device " + deviceAddress + " not found");
        }

        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            logger.d("ble-connect:onConnectionStateChange(" + status + ", " + newState + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    bluetoothGatt.discoverServices();
                    logger.d("ble-connect:connected");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    bluetoothGatt.close();
                    logger.d("ble-connect:disconnected");
                    callback.onDisconnected();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            logger.d("ble-connect:onServicesDiscovered(" + status + ")");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                logger.d("ble-connect:discovered");
                seekCharacteristic(gatt.getServices());
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                byte[] data = characteristic.getValue();
                logger.d("ble-connect:received data, length:" + data.length);
                callback.onReceive(data);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
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
            }
            logger.d("ble-connect:service not found");
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

    }

}
