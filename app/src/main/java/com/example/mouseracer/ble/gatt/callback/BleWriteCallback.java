package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleWriteCallback extends BleCallback {
    void onWrite(byte[] data, BleDevice device);
}
