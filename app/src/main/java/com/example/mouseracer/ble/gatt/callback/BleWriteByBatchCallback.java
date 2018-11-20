package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleWriteByBatchCallback extends BleCallback {
    void writeByBatchSuccess(byte[] data, BleDevice device);
}
