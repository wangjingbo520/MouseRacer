package com.example.mouseracer.myBle.ble.proxy;

import com.example.mouseracer.myBle.ble.callback.BleConnCallback;
import com.example.mouseracer.myBle.ble.callback.BleMtuCallback;
import com.example.mouseracer.myBle.ble.callback.BleNotiftCallback;
import com.example.mouseracer.myBle.ble.callback.BleReadCallback;
import com.example.mouseracer.myBle.ble.callback.BleReadRssiCallback;
import com.example.mouseracer.myBle.ble.callback.BleScanCallback;
import com.example.mouseracer.myBle.ble.callback.BleWriteCallback;
import com.example.mouseracer.myBle.ble.callback.BleWriteEntityCallback;

/**
 *
 * Created by LiuLei on 2017/10/30.
 */

public interface RequestLisenter<T> {

    void startScan(BleScanCallback<T> callback);

    void stopScan();

    boolean connect(T device, BleConnCallback<T> callback);

    boolean connect(String address, BleConnCallback<T> callback);

    void notify(T device, BleNotiftCallback<T> callback);

    void unNotify(T device);

    void disconnect(T device);

    void disconnect(T device, BleConnCallback<T> callback);

    boolean read(T device, BleReadCallback<T> callback);

    boolean readRssi(T device, BleReadRssiCallback<T> callback);

    boolean write(T device, byte[] data, BleWriteCallback<T> callback);

    void writeEntity(T device, final byte[] data, int packLength, int delay, BleWriteEntityCallback<T> callback);

//    boolean writeAutoEntity(T device, final byte[]data, int packLength);

    boolean setMtu(String address, int mtu, BleMtuCallback<T> callback);
}
