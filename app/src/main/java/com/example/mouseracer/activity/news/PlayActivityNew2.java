package com.example.mouseracer.activity.news;

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
import com.example.mouseracer.activity.BaseActivity;
import com.example.mouseracer.activity.GuideActivity;
import com.example.mouseracer.activity.MainActivity;
import com.example.mouseracer.ble.BleManager;
import com.example.mouseracer.ble.gatt.callback.BleWriteCallback;
import com.example.mouseracer.myBle.ble.Ble;
import com.example.mouseracer.myBle.ble.BleDevice;
import com.example.mouseracer.util.BlueToothUtils;
import com.example.mouseracer.util.Constants;
import com.example.mouseracer.view.MyLinearLayout;
import com.example.mouseracer.view.VerticalSeekBar;

import java.lang.ref.WeakReference;

import static com.example.mouseracer.util.MathUtils.convertDecimalToBinary;
import static com.example.mouseracer.util.MathUtils.hexStringToBytes;


public class PlayActivityNew2 extends BaseActivity implements View.OnClickListener, VerticalSeekBar
        .SlideChangeListener, MyLinearLayout.LinearChangeListener {
    private static final String TAG = "PlayActivity";
    private VerticalSeekBar verticalSeekBar;
    private float speed = 1;
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
    private com.example.mouseracer.ble.BleDevice device;


    public static void start(Context context, com.example.mouseracer.ble.BleDevice device) {
        Intent starter = new Intent(context, PlayActivityNew2.class);
        starter.putExtra("device", device);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        device = getIntent().getParcelableExtra("device");
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
        verticalSeekBar.setMaxProgress(11);
        verticalSeekBar.setProgress(0);
        verticalSeekBar.setOnSlideChangeListener(this);
        lltop.setOnLinearChangeListener(this);
        llbottom.setOnLinearChangeListener(this);
        llleft.setOnLinearChangeListener(this);
        llright.setOnLinearChangeListener(this);
        myMainHandler = new MyMainHandler(this);
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
                this.cmd = "03";
                llbottom.setOnLinearChangeListener(null);
                llright.setOnLinearChangeListener(null);
                lltop.setOnLinearChangeListener(null);
                break;
            case R.id.llright:
                //向右
                ivright.setBackgroundResource(R.mipmap.y2);
                this.cmd = "04";
                llbottom.setOnLinearChangeListener(null);
                llleft.setOnLinearChangeListener(null);
                lltop.setOnLinearChangeListener(null);
                break;
            default:
                break;
        }
        Log.e("---------->", "精致了");
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
        speed = 1;
        verticalSeekBar.setProgress(0);
        myMainHandler.sendEmptyMessage(1);
    }

    @Override
    public void onStart(VerticalSeekBar slideView, float progress) {

    }

    @Override
    public void onProgress(VerticalSeekBar slideView, float progress) {
        if (progress > 0) {
            this.speed = progress + 1;
        }
    }

    @Override
    public void onStop(VerticalSeekBar slideView, float progress) {
        verticalSeekBar.setProgress(0);
        this.speed = 1;
    }


    public static class MyMainHandler extends Handler {
        WeakReference<PlayActivityNew2> mActivityReference;

        MyMainHandler(PlayActivityNew2 activity) {
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
                                BlueToothUtils.getClient().disconnect(device);
                                BlueToothUtils.getClient().destroy();
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
    public void play(com.example.mouseracer.ble.BleDevice device, String cmd, int speed) {
        //低八位
        String data = "05" + convertDecimalToBinary(speed) + cmd;
        BleManager.getInstance(this.getApplicationContext()).write(device, Constants.serviceUuid2, Constants.writeUiid2, hexStringToBytes(data), new BleWriteCallback() {
            @Override
            public void onWrite(byte[] data, com.example.mouseracer.ble.BleDevice device) {
            }

            @Override
            public void onFail(int failCode, String info, com.example.mouseracer.ble.BleDevice device) {
            }
        });
    }

//    public void notifi(com.example.mouseracer.ble.BleDevice device){
//        BleManager.getInstance(this.getApplicationContext()).notify(device, serviceUuid, notifyUuid, new BleNotifyCallback() {
//            @Override
//            public void onCharacteristicChanged(byte[] data, BleDevice device) {
//
//            }
//
//            @Override
//            public void onNotifySuccess(String notifySuccessUuid, BleDevice device) {
//
//            }
//
//            @Override
//            public void onFail(int failCode, String info, BleDevice device) {
//
//            }
//        });
//    }



}
