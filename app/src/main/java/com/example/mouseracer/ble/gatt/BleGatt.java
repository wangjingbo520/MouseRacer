package com.example.mouseracer.ble.gatt;


import android.bluetooth.BluetoothGatt;

import com.example.mouseracer.ble.BleDevice;
import com.example.mouseracer.ble.gatt.bean.CharacteristicInfo;
import com.example.mouseracer.ble.gatt.bean.ServiceInfo;
import com.example.mouseracer.ble.gatt.callback.BleConnectCallback;
import com.example.mouseracer.ble.gatt.callback.BleMtuCallback;
import com.example.mouseracer.ble.gatt.callback.BleNotifyCallback;
import com.example.mouseracer.ble.gatt.callback.BleReadCallback;
import com.example.mouseracer.ble.gatt.callback.BleRssiCallback;
import com.example.mouseracer.ble.gatt.callback.BleWriteByBatchCallback;
import com.example.mouseracer.ble.gatt.callback.BleWriteCallback;

import java.util.List;
import java.util.Map;

public interface BleGatt {
    void connect(int connectTimeout, BleDevice device, BleConnectCallback callback);

    void disconnect(String address);

    void disconnectAll();

    void notify(BleDevice device, String serviceUuid, String notifyUuid, BleNotifyCallback callback);

    void cancelNotify(BleDevice device, String serviceUuid, String characteristicUuid);

    void read(BleDevice device, String serviceUuid, String readUuid, BleReadCallback callback);

    void write(BleDevice device, String serviceUuid, String writeUuid, byte[] data, BleWriteCallback callback);

    void writeByBatch(BleDevice device, String serviceUuid, String writeUuid, byte[] data, int lengthPerPackage, BleWriteByBatchCallback callback);

    void readRssi(BleDevice device, BleRssiCallback callback);

    void setMtu(BleDevice device, int mtu, BleMtuCallback callback);

    List<BleDevice> getConnectedDevices();

    Map<ServiceInfo, List<CharacteristicInfo>> getDeviceServices(BleDevice device);

    BluetoothGatt getBluetoothGatt(String address);

    void destroy();
}
