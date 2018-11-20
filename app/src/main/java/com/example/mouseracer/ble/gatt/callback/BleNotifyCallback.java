package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleNotifyCallback extends BleCallback {
    void onCharacteristicChanged(byte[] data, BleDevice device);

    void onNotifySuccess(String notifySuccessUuid, BleDevice device);
}
