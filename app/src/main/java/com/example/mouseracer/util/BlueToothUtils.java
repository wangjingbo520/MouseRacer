package com.example.mouseracer.util;

import com.example.mouseracer.MyApplication;
import com.example.mouseracer.ble.BleManager;

public class BlueToothUtils {
    private static BleManager mClient;

    public static BleManager getClient() {
        if (mClient == null) {
            synchronized (BlueToothUtils.class) {
                if (mClient == null) {
                    BleManager.toggleBluetooth(true);
                    BleManager.Options options = new BleManager.Options();
                    options.loggable = true;
                    options.scanPeriod = 5000;
                    options.connectTimeout = 10000;
                    mClient = BleManager.getInstance(MyApplication.getInstance());
                    mClient.option(options);
                }
            }
        }
        return mClient;
    }
}
