package com.example.mouseracer.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.mouseracer.LeDeviceListAdapter;
import com.example.mouseracer.R;
import com.example.mouseracer.myBle.ble.Ble;
import com.example.mouseracer.myBle.ble.BleDevice;
import com.example.mouseracer.myBle.ble.callback.BleConnCallback;
import com.example.mouseracer.myBle.ble.callback.BleScanCallback;
import com.example.mouseracer.myBle.ble.callback.BleWriteCallback;
import com.example.mouseracer.util.Constants;
import com.example.mouseracer.util.MathUtils;
import com.example.mouseracer.util.ToastUtil;
import com.example.mouseracer.view.LoadingDialog;

import java.util.UUID;

import static com.example.mouseracer.util.MathUtils.makeChecksum;
import static com.example.mouseracer.util.MathUtils.randomHexString;


public class BleActivity extends BaseActivity implements LeDeviceListAdapter.DeviceSelectListenser {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private ListView listView;
    private TextView tvTitle;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Ble<BleDevice> mBle;
    private LoadingDialog.Builder builder;
    private LoadingDialog dialog;
    private BleDevice mybleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble);
        initView();
        initBle();
    }

    private void initView() {
        builder = new LoadingDialog.Builder(this);
        builder.setMessage("Connecting Device").setCancelable(false);
        dialog = builder.create();
        swipeRefreshLayout = findViewById(R.id.swipe);
        listView = findViewById(R.id.listView);
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setText("Device List");
        mLeDeviceListAdapter = new LeDeviceListAdapter(this, this);
        listView.setAdapter(mLeDeviceListAdapter);
        listView.setOnItemClickListener(onItemClickListener);
        findViewById(R.id.flBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mBle != null) {
                    mBle.startScan(scanCallback);
                }
            }
        });
    }

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BleDevice device = mLeDeviceListAdapter.getDevice(position);
            if (device == null) return;
            if (mBle.isScanning()) {
                mBle.stopScan();
            }
            if (device.isConnected()) {
                if (device.getBleName().equals("pets")) {
                    //新老鼠
                    renZhen(device);
                } else if (device.getBleName().equals("Pets Hunting")) {
                    //旧老鼠
                    PlayActivity2.start(BleActivity.this, device);
                }
            } else if (!device.isConnectting()) {
                //扫描到设备时   务必用该方式连接(是上层逻辑问题， 否则点击列表  虽然能够连接上，但设备列表的状态不会发生改变)
                dialog.show();
                mBle.connect(device, connectCallback);
                //此方式只是针对不进行扫描连接（如上，若通过该方式进行扫描列表的连接  列表状态不会发生改变）
//            mBle.connect(device.getBleAddress(), connectCallback);
            }
        }
    };


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
                .setScanPeriod(12 * 1000)
                //设置扫描时长
                .setUuid_service(UUID.fromString(Constants.serviceUuid))
                //设置主服务的uuid
                .setUuid_write_cha(UUID.fromString(Constants.writeUiid))
                //设置可写特征的uuid
                .create(getApplicationContext());
        checkBluetoothStatus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == Ble.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else if (requestCode == Ble.REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            //6、若打开，则进行扫描
            mBle.startScan(scanCallback);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void checkBluetoothStatus() {
        // 检查设备是否支持BLE4.0
        if (!mBle.isSupportBle(this)) {
            ToastUtil.showMessage(R.string.ble_not_supported);
            finish();
        }
        if (!mBle.isBleEnable()) {
            //4、若未打开，则请求打开蓝牙
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Ble.REQUEST_ENABLE_BT);
        } else {
            //5、若已打开，则进行扫描
            mBle.startScan(scanCallback);
        }
    }


    @Override
    public void onSelectDevice(BleDevice bleDevice) {
    }

    /**
     * 连接的回调
     */
    private BleConnCallback<BleDevice> connectCallback = new BleConnCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(final BleDevice device) {
            if (device.isConnected()) {
                if (device.getBleName().equals("pets")) {
                    //新老鼠
                    renZhen(device);
                } else if (device.getBleName().equals("Pets Hunting")) {
                    //旧老鼠
                    PlayActivity2.start(BleActivity.this, device);
                } else if (device.getBleName().equals("Pets")) {
                    //新老鼠
                    renZhen(device);
                }
            } else {
                dialog.dismiss();
            }
            mLeDeviceListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onConnectException(BleDevice device, int errorCode) {
            super.onConnectException(device, errorCode);
            swipeRefreshLayout.setRefreshing(false);
            dialog.dismiss();
        }
    };

    BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            //if (device.getBleName().equals("Pets Hunting") || device.getBleName().equals("pets") ) {
            mLeDeviceListAdapter.addDevice(device);
            mLeDeviceListAdapter.notifyDataSetChanged();
            //     }
        }

        @Override
        public void onStop() {
            super.onStop();
            swipeRefreshLayout.setRefreshing(false);
        }
    };

    public void renZhen(final BleDevice bleDevice) {
        this.mybleDevice = bleDevice;
        String randomString = randomHexString(12);
        String message = "5a" + randomString + makeChecksum(randomString) + "a5";
        byte[] bytes = MathUtils.hexStringToBytes(message);
        if (mBle != null) {
            boolean result = mBle.write(bleDevice, bytes, new BleWriteCallback<BleDevice>() {
                @Override
                public void onWriteSuccess(BluetoothGattCharacteristic characteristic) {
                    //   ToastUtil.showMessage("认证成功");
                    PlayActivity.start(BleActivity.this, mybleDevice);
                }
            });
            if (!result) {
                ToastUtil.showMessage("认证失败");
            }
        }
    }

}
