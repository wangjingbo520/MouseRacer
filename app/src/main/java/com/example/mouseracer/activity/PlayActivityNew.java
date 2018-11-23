package com.example.mouseracer.activity;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.example.mouseracer.R;
import com.example.mouseracer.nordic.NewMouseManager;
import com.example.mouseracer.view.BatteryView;
import com.example.mouseracer.view.MyLinearLayout;
import com.example.mouseracer.view.VerticalSeekBar;

import java.lang.ref.WeakReference;

import no.nordicsemi.android.ble.BleManagerCallbacks;

import static com.example.mouseracer.util.MathUtils.convertDecimalToBinary;
import static com.example.mouseracer.util.MathUtils.hexStringToBytes;
import static com.example.mouseracer.util.MathUtils.makeChecksum;
import static com.example.mouseracer.util.MathUtils.randomHexString;


public class PlayActivityNew extends BaseActivity implements View.OnClickListener, VerticalSeekBar
        .SlideChangeListener, MyLinearLayout.LinearChangeListener, BleManagerCallbacks {
    private static final String TAG = "PlayActivity";
    private VerticalSeekBar verticalSeekBar;
    private BatteryView horizontalBattery;
    private float speed = 8;
    private String XX = "00";
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
    private boolean isAutoDiconncet = false;


    public static void start(Context context) {
        Intent starter = new Intent(context, PlayActivityNew.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
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
        //认证协议
        String randomString = randomHexString(12);
        String message = "5a" + randomString + makeChecksum(randomString) + "a5";
        byte[] bytes = hexStringToBytes(message);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NewMouseManager.getInstance(this).setBattery(horizontalBattery);
        NewMouseManager.getInstance(this).writeData(bytes);
        NewMouseManager.getInstance(this).setGattCallbacks(this);
    }

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


    private final Runnable task = new Runnable() {
        @Override
        public void run() {
            myMainHandler.sendEmptyMessage(1);
        }
    };

    @Override
    public void onDeviceConnecting(BluetoothDevice device) {

    }

    @Override
    public void onDeviceConnected(BluetoothDevice device) {
        String randomString = randomHexString(12);
        String message = "5a" + randomString + makeChecksum(randomString) + "a5";
        byte[] bytes = hexStringToBytes(message);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        NewMouseManager.getInstance(this).setBattery(horizontalBattery);
        NewMouseManager.getInstance(this).writeData(bytes);
    }

    @Override
    public void onDeviceDisconnecting(BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(BluetoothDevice device) {
        if (!isAutoDiconncet) {
            NewMouseManager.getInstance(this).connect(device);
        } else {
            NewMouseManager.getInstance(PlayActivityNew.this).close();
            finish();
        }
    }

    @Override
    public void onLinklossOccur(BluetoothDevice device) {

    }


    @Override
    public void onServicesDiscovered(BluetoothDevice device, boolean optionalServicesFound) {

    }

    @Override
    public void onDeviceReady(BluetoothDevice device) {

    }

    @Override
    public boolean shouldEnableBatteryLevelNotifications(BluetoothDevice device) {
        return false;
    }

    @Override
    public void onBatteryValueReceived(BluetoothDevice device, int value) {

    }

    @Override
    public void onBondingRequired(BluetoothDevice device) {

    }

    @Override
    public void onBonded(BluetoothDevice device) {

    }


    @Override
    public void onError(BluetoothDevice device, String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(BluetoothDevice device) {

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
                    mActivityReference.get().writeData(mActivityReference.get().cmd, (int) mActivityReference.get()
                            .speed);
                    mActivityReference.get().myMainHandler.postDelayed(mActivityReference.get()
                            .task, 50);
                    break;
                default:
                    break;
            }
        }
    }

    private void writeData(String cmd, int speed) {
        String crc = makeChecksum(cmd + convertDecimalToBinary(speed) + XX);
        String data = "5a" + cmd + convertDecimalToBinary(speed) + XX + crc + "a5";
        NewMouseManager.getInstance(this).writeData(hexStringToBytes(data));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            new AlertDialog.Builder(this)
                    .setMessage("The device will be disconnected,Are you sure to quit?")
                    .setPositiveButton("sure", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            isAutoDiconncet = true;
                            myMainHandler.removeCallbacks(task);
                            NewMouseManager.getInstance(PlayActivityNew.this).disconnect();
//                            NewMouseManager.getInstance(PlayActivityNew.this).close();
//                            finish();
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

}
