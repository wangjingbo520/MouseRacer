package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

public interface BleMtuCallback extends BleCallback {
    void onMtuChanged(int mtu, BleDevice device);
}
