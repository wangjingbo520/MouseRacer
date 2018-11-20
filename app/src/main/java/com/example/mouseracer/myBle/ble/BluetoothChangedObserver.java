package com.example.mouseracer.myBle.ble;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.lang.ref.WeakReference;

/**
 * 蓝牙状态发生变化时
 * Created by jerry on 2018/8/29.
 */

public class BluetoothChangedObserver {

    private BluetoothStatusLisenter mBluetoothStatusLisenter;
    private BleReceiver mBleReceiver;
    private Context mContext;

    public interface BluetoothStatusLisenter {
        void onBluetoothStatusChanged(int status);
    }

    public BluetoothChangedObserver(Context context){
        this.mContext = context;
    }

    public void setBluetoothStatusLisenter(BluetoothStatusLisenter bluetoothStatusLisenter) {
        this.mBluetoothStatusLisenter = bluetoothStatusLisenter;
    }

    public void registerReceiver() {
        mBleReceiver = new BleReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        mContext.registerReceiver(mBleReceiver, filter);
    }

    public void unregisterReceiver() {
        try {
            mContext.unregisterReceiver(mBleReceiver);
            mBluetoothStatusLisenter = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class BleReceiver extends BroadcastReceiver {
        private WeakReference<BluetoothChangedObserver> mObserverWeakReference;

        public BleReceiver(BluetoothChangedObserver bluetoothChangedObserver){
            mObserverWeakReference = new WeakReference<BluetoothChangedObserver>(bluetoothChangedObserver);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                BluetoothChangedObserver observer = mObserverWeakReference.get();
                int status = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if (status == BluetoothAdapter.STATE_ON) {
                    observer.mBluetoothStatusLisenter.onBluetoothStatusChanged(BleStates.BleStatus.BlutoothStatusOn);
                }else if(status == BluetoothAdapter.STATE_OFF){
                    observer.mBluetoothStatusLisenter.onBluetoothStatusChanged(BleStates.BleStatus.BlutoothStatusOff);
                }
            }
        }
    }
}
