package com.example.mouseracer.activity;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import com.example.mouseracer.CommonRecyclerViewAdapter;
import com.example.mouseracer.R;
import com.example.mouseracer.ScanDeviceAdapter;
import com.example.mouseracer.activity.news.PlayActivityNew;
import com.example.mouseracer.activity.news.PlayActivityNew2;
import com.example.mouseracer.ble.BleDevice;
import com.example.mouseracer.ble.BleManager;
import com.example.mouseracer.ble.gatt.callback.BleConnectCallback;
import com.example.mouseracer.ble.gatt.callback.BleWriteCallback;
import com.example.mouseracer.ble.scan.BleScanCallback;
import com.example.mouseracer.util.Constants;
import com.example.mouseracer.util.MathUtils;
import com.example.mouseracer.util.ToastUtil;
import com.example.mouseracer.view.LoadingDialog;

import java.util.ArrayList;
import java.util.List;

import static com.example.mouseracer.util.MathUtils.makeChecksum;
import static com.example.mouseracer.util.MathUtils.randomHexString;

public class ScanMainActivity extends Activity {
    private static final String TAG = "------------->";
    private RecyclerView recyclerview;
    private List<BleDevice> deviceList = new ArrayList<>();
    private ScanDeviceAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LoadingDialog.Builder builder;
    private LoadingDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_main);
        initView();
        initBleManager();
    }

    private void initView() {
        builder = new LoadingDialog.Builder(this);
        builder.setMessage("Connecting Device").setCancelable(false);
        dialog = builder.create();
        recyclerview = findViewById(R.id.recyclerview);
        swipeRefreshLayout = findViewById(R.id.swipe);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                outRect.top = 3;
            }
        });
        SparseArray<int[]> res = new SparseArray<>();
        res.put(R.layout.item_rv_scan_devices, new int[]{R.id.tv_name, R.id.tv_address, R.id.tv_connection_state});
        adapter = new ScanDeviceAdapter(this, deviceList, res);
        adapter.setOnItemClickListener(new CommonRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                BleManager.getInstance(ScanMainActivity.this).stopScan();
                BleManager.getInstance(ScanMainActivity.this).connect(deviceList.get(position), new BleConnectCallback() {
                    @Override
                    public void onStart(boolean startConnectSuccess, String info, BleDevice device) {
                        dialog.show();
                        Log.e(TAG, "start connecting = " + startConnectSuccess + "     info: " + info);
                    }

                    @Override
                    public void onTimeout(BleDevice device) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        Toast.makeText(ScanMainActivity.this, "connect timeout!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnected(BleDevice device) {
                        adapter.notifyDataSetChanged();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        if (device.name.equals("pets")) {
                            writeData(device);
                        } else {
                            PlayActivityNew2.start(ScanMainActivity.this, device);
                        }
                    }

                    @Override
                    public void onDisconnected(BleDevice device) {
                        adapter.notifyDataSetChanged();
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        ToastUtil.showMessage("connect failed");
                    }
                });
            }
        });
        recyclerview.setAdapter(adapter);
        findViewById(R.id.flBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startScan();
            }
        });
    }

    private void initBleManager() {
        //check if this android device supports ble
        if (!BleManager.supportBle(this)) {
            return;
        }
        //open bluetooth without a request dialog
        BleManager.toggleBluetooth(true);
        if (!BleManager.isBluetoothOn()) {
            BleManager.toggleBluetooth(true);
        }
        startScan();
    }

    private void startScan() {
        BleManager.getInstance(this.getApplicationContext()).startScan(new BleScanCallback() {
            @Override
            public void onLeScan(BleDevice device, int rssi, byte[] scanRecord) {
                if (device.name.equals("pets") || device.name.equals("Pets Hunting")) {
                    for (BleDevice d : deviceList) {
                        if (device.address.equals(d.address)) {
                            return;
                        }
                    }
                } else {
                    return;
                }
                deviceList.add(device);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onStart(boolean startScanSuccess, String info) {
                Log.e(TAG, "start scan = " + startScanSuccess + "   info: " + info);
                if (startScanSuccess) {
                    deviceList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFinish() {
                swipeRefreshLayout.setRefreshing(false);
                Log.e(TAG, "scan finish");
            }
        });
    }

    private void writeData(BleDevice device) {
        String randomString = randomHexString(12);
        String message = "5a" + randomString + makeChecksum(randomString) + "a5";
        byte[] bytes = MathUtils.hexStringToBytes(message);
        BleManager.getInstance(ScanMainActivity.this).write(device, Constants.serviceUuid, Constants.writeUiid, bytes, new BleWriteCallback() {
            @Override
            public void onWrite(byte[] data, BleDevice device) {
                PlayActivityNew.start(ScanMainActivity.this, device);
            }

            @Override
            public void onFail(int failCode, String info, BleDevice device) {
                Toast.makeText(ScanMainActivity.this, "write fail: " + info, Toast.LENGTH_SHORT).show();
            }
        });

    }

}
