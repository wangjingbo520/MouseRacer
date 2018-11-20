package com.example.mouseracer.myBle.ble.callback;


import com.example.mouseracer.myBle.ble.BleDevice;

/**
 * Created by LiuLei on 2018/6/2.
 */

public abstract class BleMtuCallback<T> {

    public void onMtuChanged(BleDevice device, int mtu, int status){}

}
