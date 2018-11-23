package com.example.mouseracer.nordic;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;

import com.example.mouseracer.util.Constants;
import com.example.mouseracer.util.MathUtils;
import com.example.mouseracer.view.BatteryView;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.Request;

public class NewMouseManager extends BleManager {
    final private UUID APP_SERVICE_UUID = UUID.fromString(Constants.serviceUuid);
    final private UUID APP_WRITE_CS_UUID = UUID.fromString(Constants.writeUiid);
    final private UUID APP_NOTIFY_CS_UUID = UUID.fromString(Constants.notifiUuid);

    private BluetoothGattCharacteristic appWriteCs, appNotifyCs;

    private static NewMouseManager ourInstance = null;
    private byte[] bytes;
    private BatteryView battery;

    public static synchronized NewMouseManager getInstance(final Context context) {
        if (ourInstance == null) {
            ourInstance = new NewMouseManager(context);
        }
        return ourInstance;
    }

    private NewMouseManager(final Context context) {
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
            byte[] value = characteristic.getValue();
            if (value[0] == 0x4b) {
                //电量
                bytes = MathUtils.hexStringToBytes(Arrays.toString(characteristic.getValue()));
                Integer x = Integer.parseInt(String.valueOf(bytes[1]), 16);
                if (battery != null) {
                    battery.setPower(x * 10);
                }
            }

        }
    };

    public void setBattery(BatteryView battery) {
        this.battery = battery;
    }


    public void writeData(byte[] data) {
        if (appNotifyCs == null) {
            return;
        }
        writeCharacteristic(appWriteCs, data);
    }

}


