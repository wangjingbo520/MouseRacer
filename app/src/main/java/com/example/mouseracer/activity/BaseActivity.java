package com.example.mouseracer.activity;

import android.support.v7.app.AppCompatActivity;

/**
 * @author bobo
 * @date 2018/9/22
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "---------->";
//    public void play2(BleDevice device, String cmd, int speed) {
//        //低八位
//        String data = "05" + convertDecimalToBinary(speed) + cmd;
//        if (mBle != null) {
//            boolean result = mBle.write(device, hexStringToBytes(data), new BleWriteCallback<BleDevice>() {
//                @Override
//                public void onWriteSuccess(BluetoothGattCharacteristic characteristic) {
//                    Log.e(TAG, "发送数据成功 ");
//                }
//            });
//            if (!result) {
//                Log.e(TAG, "changeLevelInner: " + "发送数据失败!");
//            }
//        }
//    }


}
