package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleRssiCallback extends BleCallback {

    void onRssi(int rssi, BleDevice bleDevice);
}
