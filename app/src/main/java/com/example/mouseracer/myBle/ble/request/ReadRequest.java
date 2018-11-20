package com.example.mouseracer.myBle.ble.request;

import android.bluetooth.BluetoothGattCharacteristic;
import android.os.Message;

import com.example.mouseracer.myBle.ble.Ble;
import com.example.mouseracer.myBle.ble.BleDevice;
import com.example.mouseracer.myBle.ble.BleHandler;
import com.example.mouseracer.myBle.ble.BleStates;
import com.example.mouseracer.myBle.ble.BluetoothLeService;
import com.example.mouseracer.myBle.ble.annotation.Implement;
import com.example.mouseracer.myBle.ble.callback.BleReadCallback;


/**
 *
 * Created by LiuLei on 2017/10/23.
 */
@Implement(ReadRequest.class)
public class ReadRequest<T extends BleDevice> implements IMessage {

    private BleReadCallback<T> mBleLisenter;

    protected ReadRequest() {
        BleHandler handler = BleHandler.getHandler();
        handler.setHandlerCallback(this);
    }

    public boolean read(T device, BleReadCallback<T> lisenter){
        this.mBleLisenter = lisenter;
        boolean result = false;
        BluetoothLeService service = Ble.getInstance().getBleService();
        if (Ble.getInstance() != null && service != null) {
            result = service.readCharacteristic(device.getBleAddress());
        }
        return result;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what){
            case BleStates.BleStatus.Read:
                if(msg.obj instanceof BluetoothGattCharacteristic){
                    BluetoothGattCharacteristic characteristic = (BluetoothGattCharacteristic) msg.obj;
                    if(mBleLisenter != null){
                        mBleLisenter.onReadSuccess(characteristic);
                    }
                }
                break;
            default:
                break;
        }
    }
}
