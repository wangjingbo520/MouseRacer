package com.example.mouseracer.ble.gatt.callback;


import com.example.mouseracer.ble.BleDevice;

/**
 * Created by pw on 2018/9/13.
 */

public interface BleCallback {
    int FAIL_DISCONNECTED = 200;
    int FAIL_OTHER = 201;

    void onFail(int failCode, String info, BleDevice device);
}
