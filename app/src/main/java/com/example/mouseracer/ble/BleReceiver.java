package com.example.mouseracer.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pw on 2018/9/23.
 */

public class BleReceiver extends BroadcastReceiver {
    private List<BluetoothStateChangedListener> listeners = new ArrayList<>();

    public static BleReceiver getInstance() {
        return BleReceiverHolder.sBleReceiver;
    }

    private static class BleReceiverHolder {
        static final BleReceiver sBleReceiver = new BleReceiver();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                for (BluetoothStateChangedListener l : listeners) {
                    if (l != null) {
                        l.onBluetoothStateChanged();
                    }
                }
                break;
            case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                for (BluetoothStateChangedListener l : listeners) {
                    if (l != null) {
                        l.onConnectStatusCHnaged(device.getAddress());
                    }
                }
             //   Log.e("------------>", "onReceive: " + "蓝牙已断开了" + device.getAddress() + device.getAddress());
                break;
            default:
                break;
        }
    }

    public synchronized void registerBluetoothStateChangedListener(BluetoothStateChangedListener listener) {
        checkNotNull(listener, BluetoothStateChangedListener.class);
        listeners.add(listener);
    }

    public synchronized void unregisterBluetoothStateChangedListener(BluetoothStateChangedListener listener) {
        checkNotNull(listener, BluetoothStateChangedListener.class);
        listeners.remove(listener);
    }

    public synchronized void registerBluetoothConncetStateChangedListener(BluetoothStateChangedListener listener) {
        checkNotNull(listener, BluetoothStateChangedListener.class);
        listeners.add(listener);
    }

    public synchronized void unregisterBluetoothConncetStateChangedListener(BluetoothStateChangedListener listener) {
        checkNotNull(listener, BluetoothStateChangedListener.class);
        listeners.remove(listener);
    }

    private void checkNotNull(Object object, Class<?> clasz) {
        if (object == null) {
            String claszSimpleName = clasz.getSimpleName();
            throw new IllegalArgumentException(claszSimpleName + " is null");
        }
    }

    public interface BluetoothStateChangedListener {
        void onBluetoothStateChanged();

        void onConnectStatusCHnaged(String macAdress);
    }
}
