package com.example.mouseracer.nordic;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.mouseracer.util.Constants;

import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;

public class EV07BManager extends BleManager {

    final private UUID APP_SERVICE_UUID = UUID.fromString(Constants.serviceUuid);
    final private UUID APP_WRITE_CS_UUID = UUID.fromString(Constants.writeUiid);
    final private UUID APP_NOTIFY_CS_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    private BluetoothGattCharacteristic appWriteCs, appNotifyCs;

    private static EV07BManager ourInstance = null;


    public static synchronized EV07BManager getInstance(final Context context) {
        if (ourInstance == null) {
            ourInstance = new EV07BManager(context);
        }

        return ourInstance;
    }

    private EV07BManager(final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    @Override
    protected boolean shouldAutoConnect() {
        return true;
    }

    private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

        @Override
        protected Deque<Request> initGatt(final BluetoothGatt gatt) {
            final LinkedList<Request> requests = new LinkedList<>();
            requests.add(Request.newEnableNotificationsRequest(appNotifyCs));
            return requests;
        }

        @Override
        protected boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
            BluetoothGattService service = gatt.getService(APP_SERVICE_UUID);
            if (service != null) {
                appWriteCs = service.getCharacteristic(APP_WRITE_CS_UUID);
                appNotifyCs = service.getCharacteristic(APP_NOTIFY_CS_UUID);
            }
            return appWriteCs != null && appNotifyCs != null;
        }

        @Override
        protected void onDeviceDisconnected() {
            appWriteCs = null;
            appNotifyCs = null;
        }

        @Override
        protected void onCharacteristicNotified(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        }
    };

}


