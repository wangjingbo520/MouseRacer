package com.example.mouseracer.ble.scan;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.text.TextUtils;

import com.example.mouseracer.ble.BleDevice;
import com.example.mouseracer.ble.BleReceiver;
import com.example.mouseracer.ble.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BleScanner implements BleScan<BleScanCallback>, BleReceiver.BluetoothStateChangedListener {
    private static final String TAG = "BleScanner";

    protected BluetoothAdapter mBluetoothAdapter;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;//sdk<21 uses this scan callback
    private ScanCallback mScanCallback;//SDK>=21 uses this scan callback
    private BleScanCallback mBleScanCallback;//all sdk version uses this scan callback
    private BluetoothLeScanner mBluetoothLeScanner;
    private ScanSettings mScanSettings;
    private int mScanPeriod = 12000;//scan period, default 10s
    private String mDeviceName;
    private String mDeviceAddress;
    private UUID[] mServiceUuids;
    private volatile boolean mScanning;
    private Handler mHandler;

    public BleScanner() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = new Handler(Looper.getMainLooper());
        //register BluetoothStateChangedListener
        BleReceiver.getInstance().registerBluetoothStateChangedListener(this);
    }

    @Override
    public synchronized void startScan(int scanPeriod, String scanDeviceName, String scanDeviceAddress, UUID[] scanServiceUuids, final BleScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("BleScanCallback is null");
        }
        mBleScanCallback = callback;
        if (mScanning) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBleScanCallback != null) {
                        mBleScanCallback.onStart(false, "you can't start a new scan until the previous scan is over");
                    }
                }
            });
            return;
        }

        if (scanPeriod > 0) {
            mScanPeriod = scanPeriod;
        }
        mDeviceName = scanDeviceName;
        mDeviceAddress = scanDeviceAddress;
        mServiceUuids = scanServiceUuids;

        boolean scanStart;
        if (sdkVersionLowerThan21()) {
            scanStart = scanByOldApi();
        } else {
            scanStart = scanByNewApi();
        }
        if (scanStart) {
            mScanning = true;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBleScanCallback != null) {
                        mBleScanCallback.onStart(true, "scan begin success");
                    }
                }
            });
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScan();
                }
            }, mScanPeriod);
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mBleScanCallback != null) {
                        mBleScanCallback.onStart(false,
                                "scan begin fail,bluetooth is closed or other unknown reason");
                    }
                }
            });
        }
    }

    @SuppressWarnings("NewApi")
    @Override
    public synchronized void stopScan() {
        if (mBluetoothAdapter == null || !mScanning) {
            return;
        }
        if (sdkVersionLowerThan21()) {
            if (mLeScanCallback != null) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
        } else {
            //if bluetooth is close, stopScan() will throw an exception
            if (mBluetoothLeScanner != null && mBluetoothAdapter.isEnabled() && mScanCallback != null) {
                mBluetoothLeScanner.stopScan(mScanCallback);
            }
        }
        mScanning = false;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mBleScanCallback != null) {
                    mBleScanCallback.onFinish();
                }
            }
        });
    }

    @Override
    public boolean isScanning() {
        return mScanning;
    }

    @Override
    public void onBluetoothStateChanged() {
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
            stopScan();
        }
    }

    @Override
    public void destroy() {
        stopScan();
        //remove scan period delayed message
        mHandler.removeCallbacksAndMessages(null);
        //unregister BluetoothStateChangedListener
        BleReceiver.getInstance().unregisterBluetoothStateChangedListener(this);
    }

    private boolean scanByOldApi() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return false;
        }
        if (mLeScanCallback == null) {
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!TextUtils.isEmpty(mDeviceName) && !mDeviceName.equals(device.getName())) {
                                return;
                            }
                            if (!TextUtils.isEmpty(mDeviceAddress) && !mDeviceAddress.equals(device.getAddress())) {
                                return;
                            }
                            if (mBleScanCallback != null) {
                                mBleScanCallback.onLeScan(newBleDevice(device), rssi, scanRecord);
                            }
                        }
                    });
                }
            };
        }
        return mBluetoothAdapter.startLeScan(mServiceUuids, mLeScanCallback);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean scanByNewApi() {
        //mBluetoothLeScanner will be null and startScan() will throw an exception if bluetooth isn't open
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            return false;
        }
        if (mScanCallback == null) {
            mScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
                    super.onScanResult(callbackType, result);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (!hasResultByFilterUuids(result)) return;
                            if (mBleScanCallback == null) return;
                            byte[] scanRecord = (result.getScanRecord() == null) ? new byte[]{} : result.getScanRecord().getBytes();
                            mBleScanCallback.onLeScan(newBleDevice(result.getDevice()), result.getRssi(), scanRecord);
                        }
                    });
                }

                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult sr : results) {
                        Logger.i("Batch scan results: " + sr.toString());
                    }
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Logger.i("Ble scan fail: " + errorCode);
                }
            };
        }
        //note that getBluetoothLeScanner() will be null if bluetooth is closed
        if (mBluetoothLeScanner == null) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        }
        if (mScanSettings == null) {
            mScanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
        }
        List<ScanFilter> scanFilters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setDeviceName(mDeviceName)
                .setDeviceAddress(mDeviceAddress)
                .build();
        scanFilters.add(filter);
        mBluetoothLeScanner.startScan(scanFilters, mScanSettings, mScanCallback);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean hasResultByFilterUuids(ScanResult result) {
        if (mServiceUuids == null || mServiceUuids.length <= 0) {//no filtered uuids
            return true;
        }
        ScanRecord scanRecord = result.getScanRecord();
        if (scanRecord == null) {
            return false;
        }
        List<ParcelUuid> serviceUuidList = new ArrayList<>();
        for (UUID uuid : mServiceUuids) {
            serviceUuidList.add(new ParcelUuid(uuid));
        }
        List<ParcelUuid> scanServiceUuids = result.getScanRecord().getServiceUuids();
        return scanServiceUuids != null && scanServiceUuids.containsAll(serviceUuidList);
    }

    private boolean sdkVersionLowerThan21() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP;
    }

    private BleDevice newBleDevice(BluetoothDevice device) {
        Class<?> clasz = BleDevice.class;
        try {
            Constructor<?> constructor = clasz.getDeclaredConstructor(BluetoothDevice.class);
            constructor.setAccessible(true);
            BleDevice bleDevice = (BleDevice) constructor.newInstance(device);
            return bleDevice;
        } catch (Exception e) {
            Logger.i("encounter an exception while creating a BleDevice object by reflection: " + e.getMessage());
            return null;
        }
    }
}
