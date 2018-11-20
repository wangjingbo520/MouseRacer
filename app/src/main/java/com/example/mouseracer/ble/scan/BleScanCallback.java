package com.example.mouseracer.ble.scan;


import com.example.mouseracer.ble.BleDevice;

public interface BleScanCallback {
    void onLeScan(BleDevice device, int rssi, byte[] scanRecord);

    void onStart(boolean startScanSuccess, String info);

    void onFinish();
}
