package com.example.mouseracer.activity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.mouseracer.R;
import com.example.mouseracer.myBle.ble.Ble;
import com.example.mouseracer.myBle.ble.BleDevice;
import com.example.mouseracer.myBle.ble.callback.BleNotiftCallback;
import com.example.mouseracer.myBle.ble.callback.BleWriteCallback;
import com.example.mouseracer.util.MathUtils;
import com.example.mouseracer.util.ToastUtil;
import com.example.mouseracer.view.BatteryView;
import com.example.mouseracer.view.MyLinearLayout;
import com.example.mouseracer.view.VerticalSeekBar;

import java.lang.ref.WeakReference;
import java.util.Arrays;

import static com.example.mouseracer.util.MathUtils.convertDecimalToBinary;
import static com.example.mouseracer.util.MathUtils.hexStringToBytes;
import static com.example.mouseracer.util.MathUtils.makeChecksum;
import static com.example.mouseracer.util.MathUtils.randomHexString;


public class PlayActivityNew extends BaseActivity implements View.OnClickListener, VerticalSeekBar
        .SlideChangeListener, MyLinearLayout.LinearChangeListener {
    private static final String TAG = "PlayActivity";
    private VerticalSeekBar verticalSeekBar;
    private BatteryView horizontalBattery;
    private float speed = 8;
    private String XX = "00";
    private Ble<BleDevice> mBle;
    /**
     * 静止的命令
     */
    public static String STILL_CODE = "00";
    private String cmd = STILL_CODE;
    private MyMainHandler myMainHandler;

    private ImageView ivUp;
    private ImageView ivdown;
    private ImageView ivright;
    private ImageView ivleft;

    private MyLinearLayout lltop;
    private MyLinearLayout llleft;
    private MyLinearLayout llbottom;
    private MyLinearLayout llright;
    private BleDevice device;
    private byte[] bytes;

    public static void start(Context context, BleDevice device) {
        Intent starter = new Intent(context, PlayActivityNew.class);
        starter.putExtra("device", device);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        device = (BleDevice) getIntent().getSerializableExtra("device");
        initBle();
        verticalSeekBar = findViewById(R.id.verticalSeekBar);
        findViewById(R.id.llguide).setOnClickListener(this);
        findViewById(R.id.llhome).setOnClickListener(this);

        lltop = findViewById(R.id.lltop);
        llleft = findViewById(R.id.llleft);
        llbottom = findViewById(R.id.llbottom);
        llright = findViewById(R.id.llright);
        ivUp = findViewById(R.id.ivUp);
        ivdown = findViewById(R.id.ivdown);
        ivright = findViewById(R.id.ivright);
        ivleft = findViewById(R.id.ivleft);

        verticalSeekBar = findViewById(R.id.verticalSeekBar);
        horizontalBattery = findViewById(R.id.horizontalBattery);
        verticalSeekBar.setMaxProgress(11);
        verticalSeekBar.setProgress(0);
        verticalSeekBar.setOnSlideChangeListener(this);
        lltop.setOnLinearChangeListener(this);
        llbottom.setOnLinearChangeListener(this);
        llleft.setOnLinearChangeListener(this);
        llright.setOnLinearChangeListener(this);
        myMainHandler = new MyMainHandler(this);
    }

    private void initBle() {
        mBle = Ble.options()
                .setLogBleExceptions(true)
                //设置是否输出打印蓝牙日志
                .setThrowBleException(true)
                //设置是否抛出蓝牙异常
                .setAutoConnect(true)
                //设置是否自动连接
                .setConnectFailedRetryCount(3)
                .setConnectTimeout(10 * 1000)
                //设置连接超时时长
                .setScanPeriod(5 * 1000)
                //设置扫描时长
                .create(this);
        renZhen(device);
    }

    BleNotiftCallback<BleDevice> bleDeviceBleNotiftCallback = new BleNotiftCallback<BleDevice>() {
        @Override
        public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
            bytes = MathUtils.hexStringToBytes(Arrays.toString(characteristic.getValue()));
            Integer x = Integer.parseInt(String.valueOf(bytes[1]), 16);
            horizontalBattery.setPower(x * 10);
        }

        @Override
        public void onReady(BleDevice device) {
            Log.e("------->", "onReady: ");
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt) {
            Log.e("------->", "onServicesDiscovered is success ");
        }

        @Override
        public void onNotifySuccess(BluetoothGatt gatt) {
            Log.e("------->", "onNotifySuccess is success ");
        }
    };

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.llguide:
                startActivity(new Intent(this, GuideActivity.class));
                break;
            case R.id.llhome:
                startActivity(new Intent(this, MainActivity.class));
                break;
            default:
                break;
        }
    }

    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            myMainHandler.sendEmptyMessage(1);
        }
    };

    @Override
    public void onStart(MyLinearLayout linearLayout) {
        myMainHandler.removeCallbacks(task);
        switch (linearLayout.getId()) {
            case R.id.lltop:
                //前进
                ivUp.setBackgroundResource(R.mipmap.s2);
                this.cmd = "01";
                llleft.setOnLinearChangeListener(null);
                llright.setOnLinearChangeListener(null);
                llbottom.setOnLinearChangeListener(null);
                break;
            case R.id.llbottom:
                //后退
                ivdown.setBackgroundResource(R.mipmap.x2);
                this.cmd = "02";
                llleft.setOnLinearChangeListener(null);
                llright.setOnLinearChangeListener(null);
                lltop.setOnLinearChangeListener(null);
                break;
            case R.id.llleft:
                //向左
                ivleft.setBackgroundResource(R.mipmap.z2);
                this.cmd = "04";
                llbottom.setOnLinearChangeListener(null);
                llright.setOnLinearChangeListener(null);
                lltop.setOnLinearChangeListener(null);
                break;
            case R.id.llright:
                //向右
                ivright.setBackgroundResource(R.mipmap.y2);
                this.cmd = "08";
                llbottom.setOnLinearChangeListener(null);
                llleft.setOnLinearChangeListener(null);
                lltop.setOnLinearChangeListener(null);
                break;
            default:
                break;
        }
        myMainHandler.sendEmptyMessage(1);
    }

    @Override
    public void onStop(MyLinearLayout linearLayout) {
        myMainHandler.removeCallbacks(task);
        switch (linearLayout.getId()) {
            case R.id.lltop:
                //前进
                ivUp.setBackgroundResource(R.mipmap.s1);
                llleft.setOnLinearChangeListener(this);
                llbottom.setOnLinearChangeListener(this);
                llright.setOnLinearChangeListener(this);
                break;
            case R.id.llbottom:
                //后退
                ivdown.setBackgroundResource(R.mipmap.x1);
                llleft.setOnLinearChangeListener(this);
                lltop.setOnLinearChangeListener(this);
                llright.setOnLinearChangeListener(this);
                break;
            case R.id.llleft:
                //向左
                ivleft.setBackgroundResource(R.mipmap.l1);
                llbottom.setOnLinearChangeListener(this);
                lltop.setOnLinearChangeListener(this);
                llright.setOnLinearChangeListener(this);
                break;
            case R.id.llright:
                //向右
                ivright.setBackgroundResource(R.mipmap.r1);
                llbottom.setOnLinearChangeListener(this);
                lltop.setOnLinearChangeListener(this);
                llleft.setOnLinearChangeListener(this);
                break;
            default:
                break;
        }
        this.cmd = STILL_CODE;
        speed = 8;
        verticalSeekBar.setProgress(0);
        myMainHandler.sendEmptyMessage(1);
    }

    @Override
    public void onStart(VerticalSeekBar slideView, float progress) {

    }

    @Override
    public void onProgress(VerticalSeekBar slideView, float progress) {
        if (progress > 0) {
            this.speed = progress + 8;
        }
    }

    @Override
    public void onStop(VerticalSeekBar slideView, float progress) {
        verticalSeekBar.setProgress(0);
        this.speed = 8;
    }


    public static class MyMainHandler extends Handler {
        WeakReference<PlayActivityNew> mActivityReference;

        MyMainHandler(PlayActivityNew activity) {
            mActivityReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivityReference.get() == null) {
                return;
            }
            switch (msg.what) {
                case 1:
                    mActivityReference.get().play(mActivityReference.get()
                            .device, mActivityReference.get().cmd, (int) mActivityReference.get()
                            .speed);
                    mActivityReference.get().myMainHandler.postDelayed(mActivityReference.get()
                            .task, 100);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage("The device will be disconnected,Are you sure to quit?")
                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (device != null) {
                                mBle.disconnect(device);
                            }
                            finish();
                        }
                    }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).setCancelable(false).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null && myMainHandler != null) {
            myMainHandler.removeCallbacks(task);
            myMainHandler.removeCallbacksAndMessages(null);
        }
    }

    /*****************************bluetooth**********************************************/
    public void play(BleDevice device, String cmd, int speed) {
        //低八位
        String crc = makeChecksum(cmd + convertDecimalToBinary(speed) + XX);
        String data = "5a" + cmd + convertDecimalToBinary(speed) + XX + crc + "a5";
        if (cmd.equals("01")) {
            Log.e(TAG, "前进数据 " + data);
        }
        if (mBle != null) {
            boolean result = mBle.write(device, hexStringToBytes(data), new BleWriteCallback<BleDevice>() {
                @Override
                public void onWriteSuccess(BluetoothGattCharacteristic characteristic) {
                    Log.e(TAG, "发送数据成功 ");
                }
            });
            if (!result) {
                Log.e(TAG, "changeLevelInner: " + "发送数据失败!");
            }
        }
    }

    /**
     * 认证协议
     */
    public void renZhen(final BleDevice bleDevice) {
        String randomString = randomHexString(12);
        String message = "5a" + randomString + makeChecksum(randomString) + "a5";
        byte[] bytes = MathUtils.hexStringToBytes(message);
        if (mBle != null) {
            boolean result = mBle.write(bleDevice, bytes, new BleWriteCallback<BleDevice>() {
                @Override
                public void onWriteSuccess(BluetoothGattCharacteristic characteristic) {
                    mBle.startNotify(device, bleDeviceBleNotiftCallback);
                }
            });
            if (!result) {
                ToastUtil.showMessage("认证失败");
            }
        }
    }

}
