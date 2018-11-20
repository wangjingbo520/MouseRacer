package com.example.mouseracer.myBle.ble.callback;

/**
 *
 * Created by LiuLei on 2017/10/23.
 */

public interface BleWriteEntityCallback<T> {
    void onWriteSuccess();
    void onWriteFailed();
}
