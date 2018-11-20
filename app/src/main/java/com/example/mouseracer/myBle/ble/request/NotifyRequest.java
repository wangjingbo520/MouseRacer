package com.example.mouseracer.myBle.ble.request;

import android.bluetooth.BluetoothGatt;
import android.os.Message;

import com.example.mouseracer.myBle.ble.BleDevice;
import com.example.mouseracer.myBle.ble.BleHandler;
import com.example.mouseracer.myBle.ble.BleStates;
import com.example.mouseracer.myBle.ble.L;
import com.example.mouseracer.myBle.ble.annotation.Implement;
import com.example.mouseracer.myBle.ble.callback.BleNotiftCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by LiuLei on 2017/10/23.
 */
@Implement(NotifyRequest.class)
public class NotifyRequest<T extends BleDevice> implements IMessage {

    private static final String TAG = "NotifyRequest";

    private BleNotiftCallback<T> mBleLisenter;
    private HashMap<T, BleNotiftCallback> mBleNotifyMap = new HashMap<>();
    private List<BleNotiftCallback> mNotifyCallbacks = new ArrayList<>();
    private HashMap<T, List<BleNotiftCallback>> mBleNotifyMaps = new HashMap<>();

    protected NotifyRequest() {
        BleHandler handler = BleHandler.getHandler();
        handler.setHandlerCallback(this);
    }

    public void notify(T device, BleNotiftCallback<T> callback) {
//        if(callback != null && !mNotifyCallbacks.contains(callback)){
//            this.mNotifyCallbacks.add(callback);
//        }
//        if(!mBleNotifyMap.containsKey(device)){
//            this.mBleNotifyMap.put(device, callback);
//            this.mNotifyCallbacks.add(callback);
//        }
        if (!mNotifyCallbacks.contains(callback)) {
            List<BleNotiftCallback> bleCallbacks;
            if (mBleNotifyMaps.containsKey(device)) {
                bleCallbacks = mBleNotifyMaps.get(device);
                bleCallbacks.add(callback);
            } else {//不包含key
                bleCallbacks = new ArrayList<>();
                bleCallbacks.add(callback);
                mBleNotifyMaps.put(device, bleCallbacks);
            }
            mNotifyCallbacks.add(callback);
        }
    }

    public void unNotify(T device) {
//        if(callback != null && mNotifyCallbacks.contains(callback)){
//            this.mNotifyCallbacks.remove(callback);
//        }
//        if(mBleNotifyMap.containsKey(device)){
//            mNotifyCallbacks.remove(mBleNotifyMap.get(device));
//            mBleNotifyMap.remove(device);
//        }
        if (mBleNotifyMaps.containsKey(device)) {
            //移除该设备的所有通知
            mNotifyCallbacks.removeAll(mBleNotifyMaps.get(device));
            mBleNotifyMaps.remove(device);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.obj == null) return;
        switch (msg.what) {
            case BleStates.BleStatus.ServicesDiscovered:
                for (BleNotiftCallback callback : mNotifyCallbacks) {
                    callback.onServicesDiscovered((BluetoothGatt) msg.obj);
                }
                break;
            case BleStates.BleStatus.NotifySuccess:
                for (BleNotiftCallback callback : mNotifyCallbacks) {
                    callback.onNotifySuccess((BluetoothGatt) msg.obj);
                }
                break;
            case BleStates.BleStatus.Changed:
                for (BleNotiftCallback callback : mNotifyCallbacks) {
                    if (msg.obj instanceof BleDevice) {
                        BleDevice device = (BleDevice) msg.obj;
                        callback.onChanged(device, device.getNotifyCharacteristic());
                        L.e("handleMessage", "onChanged++");
                    }
                }
                break;
            default:
                break;
        }
    }
}
