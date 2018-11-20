package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleReadCallback extends BleCallback {
    void onRead(byte[] data, BleDevice device);
}
