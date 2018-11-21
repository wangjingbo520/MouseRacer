package com.example.mouseracer.nordic;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.mouseracer.R;
import com.example.mouseracer.util.ToastUtil;

import no.nordicsemi.android.ble.BleManagerCallbacks;

public class ScanActivity extends AppCompatActivity
        implements ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks {
    public static final String TAG = "ScanActivity===》";
    private EV07BManager bleManager;
    protected static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        isBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.framelayout, ScannerFragment.getInstance(null))
                .commit();
        bleManager = EV07BManager.getInstance(this);
        bleManager.setGattCallbacks(this);
    }

    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    private void isBLESupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.showMessage(R.string.no_ble);
            finish();
        }
    }

    protected boolean shouldAutoConnect() {
        return false;
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        bleManager.connect(device).useAutoConnect(shouldAutoConnect())
                .retry(3, 100)
                .enqueue();
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnecting: ");
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        ToastUtil.showMessage("連接成功");
        //   PlayActivityNew.start(this, device.getAddress());
        // bleManager.w

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceDisconnecting: ");
    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceDisconnected: ");
    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {
        Log.e("--------->", "onLinkLossOccurred: ");
    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {
        Log.e("--------->", "onServicesDiscovered: ");
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {
        Log.e("--------->", "onServicesDiscovered: ");
    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {
        Log.e("--------->", "onServicesDiscovered: ");

    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {
        Log.e("--------->", "onServicesDiscovered: ");

    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {
        Log.e("--------->", "onServicesDiscovered: ");

    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

    }


}
