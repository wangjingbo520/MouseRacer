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
import android.widget.Toast;

import com.example.mouseracer.R;
import com.example.mouseracer.activity.BaseActivity;
import com.example.mouseracer.activity.GuideActivity;
import com.example.mouseracer.activity.MainActivity;
import com.example.mouseracer.ble.BleDevice;
import com.example.mouseracer.ble.BleManager;
import com.example.mouseracer.ble.gatt.callback.BleConnectCallback;
import com.example.mouseracer.ble.gatt.callback.BleNotifyCallback;
import com.example.mouseracer.ble.gatt.callback.BleWriteCallback;
import com.example.mouseracer.util.BlueToothUtils;
import com.example.mouseracer.util.Constants;
import com.example.mouseracer.util.MathUtils;
import com.example.mouseracer.util.ToastUtil;
import com.example.mouseracer.view.BatteryView;
import com.example.mouseracer.view.MyLinearLayout;
import com.example.mouseracer.view.VerticalSeekBar;

import java.lang.ref.WeakReference;

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
        Intent starter = new Intent(context, PlayActivityNew.class);
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
        horizontalBattery = findViewById(R.id.horizontalBattery);
        verticalSeekBar.setMaxProgress(11);
        verticalSeekBar.setProgress(0);
        verticalSeekBar.setOnSlideChangeListener(this);
        lltop.setOnLinearChangeListener(this);
        llbottom.setOnLinearChangeListener(this);
        llleft.setOnLinearChangeListener(this);
        llright.setOnLinearChangeListener(this);
        myMainHandler = new MyMainHandler(this);
        notifi(device);
        //检查连接状态
     //   myMainHandler.sendEmptyMessage(0);
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

    private final Runnable taskCheckConnectSatatus = new Runnable() {
        @Override
        public void run() {
            myMainHandler.sendEmptyMessage(0);
        }
    };


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
                case 0:
                    //每隔两秒检查连接状态
                    mActivityReference.get().checkConnectStatus();
                    mActivityReference.get().myMainHandler.postDelayed(mActivityReference.get()
                            .taskCheckConnectSatatus, 2000);
                    break;
                default:
                    break;
            }
        }
    }

    private void checkConnectStatus() {
        Log.e(TAG, "checkConnectStatus: " + "执行啦");
        boolean connected = BleManager.getInstance(this).isConnected(device.address);
        if (!connected) {
            //断开连接
            Log.e(TAG, "checkConnectStatus: "+"已经断开了" );
            //   myMainHandler.removeCallbacks(taskCheckConnectSatatus);
            ToastUtil.showMessage("Disconnect! Try to reconncet");
            connect();
        } else {
            Log.e(TAG, "checkConnectStatus: "+"已经连接了" );
            ToastUtil.showMessage("已经连接了");
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
        if (task != null && myMainHandler != null && taskCheckConnectSatatus != null) {
            myMainHandler.removeCallbacks(task);
            myMainHandler.removeCallbacks(taskCheckConnectSatatus);
            myMainHandler.removeCallbacksAndMessages(null);
        }
    }

    /*****************************bluetooth**********************************************/
    public void play(com.example.mouseracer.ble.BleDevice device, String cmd, int speed) {
        //低八位
        String crc = makeChecksum(cmd + convertDecimalToBinary(speed) + XX);
        String data = "5a" + cmd + convertDecimalToBinary(speed) + XX + crc + "a5";
        BleManager.getInstance(this.getApplicationContext()).write(device, Constants.serviceUuid, Constants.writeUiid, hexStringToBytes(data), new BleWriteCallback() {
            @Override
            public void onWrite(byte[] data, com.example.mouseracer.ble.BleDevice device) {
            }

            @Override
            public void onFail(int failCode, String info, com.example.mouseracer.ble.BleDevice device) {
            }
        });
    }

    public void notifi(com.example.mouseracer.ble.BleDevice device) {
        BleManager.getInstance(this.getApplicationContext()).notify(device, Constants.serviceUuid, Constants.notifiUuid, new BleNotifyCallback() {
            @Override
            public void onCharacteristicChanged(byte[] data, com.example.mouseracer.ble.BleDevice device) {
                Integer x = Integer.parseInt(String.valueOf(data[1]), 16);
                horizontalBattery.setPower(x * 10);
            }

            @Override
            public void onNotifySuccess(String notifySuccessUuid, com.example.mouseracer.ble.BleDevice device) {

            }

            @Override
            public void onFail(int failCode, String info, com.example.mouseracer.ble.BleDevice device) {

            }
        });
    }


    private void renzhen(BleDevice device) {
        String randomString = randomHexString(12);
        String message = "5a" + randomString + makeChecksum(randomString) + "a5";
        byte[] bytes = MathUtils.hexStringToBytes(message);
        BleManager.getInstance(this).write(device, Constants.serviceUuid, Constants.writeUiid, bytes, new BleWriteCallback() {
            @Override
            public void onWrite(byte[] data, BleDevice device) {

            }

            @Override
            public void onFail(int failCode, String info, BleDevice device) {
                Toast.makeText(PlayActivityNew.this, "write fail: " + info, Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void connect() {
        BleManager.getInstance(PlayActivityNew.this).connect(device, new BleConnectCallback() {
            @Override
            public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
                Log.e(TAG, "start connecting = " + startConnectSuccess + "     info: " + info);
            }

            @Override
            public void onTimeout(BleDevice device) {
                Toast.makeText(PlayActivityNew.this, "connect timeout!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnected(BleDevice device) {
                ToastUtil.showMessage("connect sucess!");
                renzhen(device);
                myMainHandler.sendEmptyMessage(0);
            }

            @Override
            public void onDisconnected(BleDevice device) {

            }
        });
    }


}
