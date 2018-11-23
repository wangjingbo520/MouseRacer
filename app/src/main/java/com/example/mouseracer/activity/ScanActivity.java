package com.example.mouseracer.activity;

import android.app.Activity;
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
import android.widget.Toast;

import com.example.mouseracer.R;
import com.example.mouseracer.nordic.NewMouseManager;
import com.example.mouseracer.nordic.OldMouseManager;
import com.example.mouseracer.nordic.ScannerFragment;

import no.nordicsemi.android.ble.BleManagerCallbacks;

public class ScanActivity extends AppCompatActivity
        implements ScannerFragment.OnDeviceSelectedListener, BleManagerCallbacks {
    public static final String TAG = "ScanActivity===》";
    private NewMouseManager bleManager;
    private OldMouseManager oldMouseManager;
    protected static final int REQUEST_ENABLE_BT = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        ensureBLESupported();
        if (!isBLEEnabled()) {
            showBLEDialog();
        } else {
            showDialog();
        }
        bleManager = NewMouseManager.getInstance(this);
        oldMouseManager = OldMouseManager.getInstance(this);
        bleManager.setGattCallbacks(this);
        oldMouseManager.setGattCallbacks(this);
    }

    private void showDialog() {
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.framelayout, ScannerFragment.getInstance(null))
                .commit();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            finish();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            //6、若打开，则进行扫描
            showDialog();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        if (device.getName().equals("pets")) {
            if (bleManager.isConnected()) {
                PlayActivityNew.start(this);
            } else {
                bleManager.connect(device);
            }
        } else if (device.getName().equals("Pets Hunting")) {
            if (oldMouseManager.isConnected()){
                PlayActivityOld.start(this);
            }else {
                oldMouseManager.connect(device);
            }

        }
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
        Log.e(TAG, "onDeviceConnecting: ");
    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {
        if (device.getName().equals("pets")) {
            PlayActivityNew.start(this);
        } else if (device.getName().equals("Pets Hunting")) {
            PlayActivityOld.start(this);
        }
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
    public void onLinklossOccur(BluetoothDevice device) {

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
    public boolean shouldEnableBatteryLevelNotifications(BluetoothDevice device) {
        return false;
    }

    @Override
    public void onBatteryValueReceived(BluetoothDevice device, int value) {

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
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

    }

    private void ensureBLESupported() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_LONG).show();
            finish();
        }
    }

}
