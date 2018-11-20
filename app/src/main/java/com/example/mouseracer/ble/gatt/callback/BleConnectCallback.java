package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleConnectCallback {
    void onStart(boolean startConnectSuccess, String info, BleDevice device);

    void onTimeout(BleDevice device);

    void onConnected(BleDevice device);

    void onDisconnected(BleDevice device);
}
