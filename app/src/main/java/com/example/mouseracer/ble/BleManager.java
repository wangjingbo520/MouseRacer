package com.example.mouseracer.ble;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.example.mouseracer.ble.gatt.BleGatt;
import com.example.mouseracer.ble.gatt.BleGattImpl;
import com.example.mouseracer.ble.gatt.bean.CharacteristicInfo;
import com.example.mouseracer.ble.gatt.bean.ServiceInfo;
import com.example.mouseracer.ble.gatt.callback.BleConnectCallback;
import com.example.mouseracer.ble.gatt.callback.BleMtuCallback;
import com.example.mouseracer.ble.gatt.callback.BleNotifyCallback;
import com.example.mouseracer.ble.gatt.callback.BleReadCallback;
import com.example.mouseracer.ble.gatt.callback.BleRssiCallback;
import com.example.mouseracer.ble.gatt.callback.BleWriteByBatchCallback;
import com.example.mouseracer.ble.gatt.callback.BleWriteCallback;
import com.example.mouseracer.ble.scan.BleScan;
import com.example.mouseracer.ble.scan.BleScanCallback;
import com.example.mouseracer.ble.scan.BleScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BleManager {
    private Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private Options mOptions;
    private volatile BleScan<BleScanCallback> mScan;
    private volatile BleGatt mGatt;
    private static volatile BleManager instance;

    private final Object mLock1 = new Object();
    private final Object mLock2 = new Object();

    private BleManager(Context context) {
        if (mContext == null) {
            if (context == null) {
                throw new IllegalArgumentException("Context is null");
            }
            if (context instanceof Activity) {
                Logger.w("Activity Leak Risk: " + context.getClass().getSimpleName());
            }
            this.mContext = context.getApplicationContext();
            this.mOptions = new Options();
            this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            registerBleReceiver();
        }
    }

    private void registerBleReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        mContext.registerReceiver(BleReceiver.getInstance(), intentFilter);
    }

    public static BleManager getInstance(Context context) {
        if (instance == null) {
            synchronized (BleManager.class) {
                if (instance == null) {
                    instance = new BleManager(context);
                    BleManager.toggleBluetooth(true);
                    BleManager.Options options = new BleManager.Options();
                    options.loggable = true;
                    options.scanPeriod = 4000;
                    options.connectTimeout = 15000;
                    instance.option(options);
                }
            }
        }
        return instance;
    }

    public void option(Options options) {
        if (options == null) {
            options = new Options();
        }
        this.mOptions = options;
        setLoggable(options.loggable);
    }

    private void setLoggable(boolean loggalbe) {
        Logger.LOGGABLE = loggalbe;
    }

    public boolean isScanning() {
        checkBleScan();
        return mScan.isScanning();
    }

    /**
     * Scan ble device
     */
    public void startScan(BleScanCallback callback) {
        checkBleScan();
        mScan.startScan(mOptions.scanPeriod, mOptions.scanDeviceName, mOptions.scanDeviceAddress,
                mOptions.scanServiceUuids, callback);
    }

    /**
     * Stop scaning device, it's strongly recommended that you call this method
     * to stop scaning after target device has been discovered
     */
    public void stopScan() {
        checkBleScan();
        mScan.stopScan();
    }

    /**
     * Connect to the remote device
     */
    public void connect(BleDevice device, BleConnectCallback callback) {
        checkBleGatt();
        mGatt.connect(mOptions.connectTimeout, device, callback);
    }

    /**
     * Connect to remote device by address
     */
    public void connect(String address, BleConnectCallback callback) {
        checkBluetoothAddress(address);
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        BleDevice bleDevice = newBleDevice(device);
        if (bleDevice == null) {
            Logger.d("new BleDevice fail!");
            return;
        }
        connect(bleDevice, callback);
    }

    /**
     * Disconnect from the remote device
     *
     * @param device remote device
     * @throws IllegalArgumentException if the device is null
     */
    public void disconnect(BleDevice device) {
        if (device == null) {
            throw new IllegalArgumentException("BleDevice is null");
        }
        disconnect(device.address);
    }

    /**
     * Disconnect from the remote device
     *
     * @param address remote device address
     */
    public void disconnect(String address) {
        checkBluetoothAddress(address);
        checkBleGatt();
        mGatt.disconnect(address);
    }

    /**
     * Disconnect all connected devices
     */
    public void disconnectAll() {
        checkBleGatt();
        mGatt.disconnectAll();
    }

    /**
     * Listen remote device notification/indication by specific notification/indication
     * characteristic
     *
     * @param device      remote device
     * @param serviceUuid service uuid which the notification or indication uuid belongs to
     * @param notifyUuid  characteristic uuid that you wanna notify or indicate, note that
     *                    the characteristic must support notification or indication, or it
     *                    will call back onFail()
     * @param callback    notification callback
     */
    public void notify(BleDevice device, String serviceUuid, String notifyUuid, BleNotifyCallback callback) {
        checkBleGatt();
        mGatt.notify(device, serviceUuid, notifyUuid, callback);
    }

    /**
     * Cancel notification/indication
     *
     * @param device             remote device
     * @param serviceUuid        service uuid
     * @param characteristicUuid characteristic uuid you want to stop notifying or indicating
     */
    public void cancelNotify(BleDevice device, String serviceUuid, String characteristicUuid) {
        checkBleGatt();
        mGatt.cancelNotify(device, serviceUuid, characteristicUuid);
    }

    /**
     * Write data to the remote device by specific writeable characteristic
     *
     * @param device      remote device
     * @param serviceUuid serivce uuid that the writeable characteristic belongs to
     * @param writeUuid   characteristic uuid which you write data, note that the
     *                    characteristic must be writeable, or it will call back onFail()
     * @param data        data
     * @param callback    result callback
     */
    public void write(BleDevice device, String serviceUuid, String writeUuid, byte[] data,
                      BleWriteCallback callback) {
        checkBleGatt();
        mGatt.write(device, serviceUuid, writeUuid, data, callback);
    }

    /**
     * Write by batch, you can use this method to split data and deliver it to remote
     * device by batch
     *
     * @param device           remote device
     * @param serviceUuid      serivce uuid that the writeable characteristic belongs to
     * @param writeUuid        characteristic uuid which you write data, note that the
     *                         characteristic must be writeable, or it will call back onFail()
     * @param data             data
     * @param lengthPerPackage data length per package
     * @param callback         result callback
     */
    public void writeByBatch(BleDevice device, String serviceUuid, String writeUuid, byte[] data,
                             int lengthPerPackage, BleWriteByBatchCallback callback) {
        checkBleGatt();
        mGatt.writeByBatch(device, serviceUuid, writeUuid, data, lengthPerPackage, callback);
    }

    /**
     * Read data from specific readable characteristic
     *
     * @param device      remote device
     * @param serviceUuid service uuid that the readable characteristic belongs to
     * @param readUuid    characteristic uuid you wanna read, note that the characteristic
     *                    must be readable, or it will call back onFail()
     * @param callback    the read callback
     */
    public void read(BleDevice device, String serviceUuid, String readUuid, BleReadCallback callback) {
        checkBleGatt();
        mGatt.read(device, serviceUuid, readUuid, callback);
    }

    /**
     * Read the remote device rssi(Received Signal Strength Indication)
     *
     * @param device   remote device
     * @param callback result callback
     */
    public void readRssi(BleDevice device, BleRssiCallback callback) {
        checkBleGatt();
        mGatt.readRssi(device, callback);
    }

    /**
     * Set MTU (Maximum Transmission Unit)
     *
     * @param device   remote device
     * @param mtu      MTU value, rang from 23 to 512
     * @param callback result callback
     */
    public void setMtu(BleDevice device, int mtu, BleMtuCallback callback) {
        checkBleGatt();
        mGatt.setMtu(device, mtu, callback);
    }

    /**
     * Get service information which the remote device supports.
     * Note that this method will return null if this device is not connected
     *
     * @return service infomations,
     */
    public Map<ServiceInfo, List<CharacteristicInfo>> getDeviceServices(BleDevice device) {
        checkBleGatt();
        return mGatt.getDeviceServices(device);
    }

    /**
     * Get connected devices list
     *
     * @return connected devices list
     */
    public List<BleDevice> getConnectedDevices() {
        checkBleGatt();
        return mGatt.getConnectedDevices();
    }

    /**
     * Return true if the specific remote device has connected with local device
     *
     * @param address device mac
     * @return true if local device has connected to the specific remote device
     */
    public boolean isConnected(String address) {
        checkBluetoothAddress(address);
        List<BleDevice> deviceList = getConnectedDevices();
        for (BleDevice d : deviceList) {
            if (address.equals(d.address)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Once you finish bluetooth, call this method to release some resources
     */
    public void destroy() {
        if (mGatt != null) {
            mGatt.destroy();
            mGatt = null;
        }
        if (mScan != null) {
            mScan.destroy();
            mScan = null;
        }
        unregisterBleReciver();
    }

    private void unregisterBleReciver() {
        try {
            mContext.unregisterReceiver(BleReceiver.getInstance());
        } catch (Exception e) {
            Logger.i("unregistering BleReceiver encounters an exception: " + e.getMessage());
        }
    }

    /**
     * Return true if this device supports ble
     */
    public static boolean supportBle(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context is null");
        }
        return BluetoothAdapter.getDefaultAdapter() != null &&
                context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Turn on local bluetooth, calling the method will show users a request dialog
     * to grant or reject,so you can get the result from Activity#onActivityResult()
     *
     * @param activity    activity, note that to get the result wether users have granted
     *                    or rejected to enable bluetooth, you should handle the method
     *                    onActivityResult() of this activity
     * @param requestCode enable bluetooth request code
     */
    public static void enableBluetooth(Activity activity, int requestCode) {
        if (activity == null || requestCode < 0) {
            return;
        }
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(intent, requestCode);
        }
    }

    /**
     * Turn on or off local bluetooth directly without showing users a request
     * dialog.
     * Note that a request dialog may still show when you call this method, due to
     * some special Android devices' system may have been modified by manufacturers
     *
     * @param enable eanble or disable local bluetooth
     */
    public static void toggleBluetooth(boolean enable) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            return;
        }
        if (enable) {
            adapter.enable();
        } else {
            if (adapter.isEnabled()) {
                adapter.disable();
            }
        }
    }

    /**
     * Return true if local bluetooth is enabled at present
     *
     * @return true if local bluetooth is open
     */
    public static boolean isBluetoothOn() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public Options getOptions() {
        return mOptions;
    }

    /**
     * Get the BluetoothGatt object of specific remote device
     *
     * @return the BluetoothGatt object, note that it will return null if connection between
     * the central device and the remote device has not started or established.
     */
    public BluetoothGatt getBluetoothGatt(String address) {
        checkBluetoothAddress(address);
        checkBleGatt();
        return mGatt.getBluetoothGatt(address);
    }

    private void checkBleScan() {
        if (mScan == null) {
            synchronized (mLock1) {
                if (mScan == null) {
                    mScan = new BleScanner();
                }
            }
        }
    }


    private void checkBluetoothAddress(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            throw new IllegalArgumentException("Invalid address: " + address);
        }
    }

    private void checkBleGatt() {
        if (mGatt == null) {
            synchronized (mLock2) {
                if (mGatt == null) {
                    mGatt = new BleGattImpl(mContext);
                }
            }
        }
    }

    private BleDevice newBleDevice(BluetoothDevice device) {
        Class<?> clasz = BleDevice.class;
        try {
            Constructor<?> constructor = clasz.getDeclaredConstructor(BluetoothDevice.class);
            constructor.setAccessible(true);
            BleDevice bleDevice = (BleDevice) constructor.newInstance(device);
            return bleDevice;
        } catch (Exception e) {
            Logger.i("Encounter an exception while creating a BleDevice object by reflection: " + e.getMessage());
            return null;
        }
    }

    public static final class Options {
        public int scanPeriod = 12000;
        public String scanDeviceName;
        public String scanDeviceAddress;
        public UUID[] scanServiceUuids;
        public int connectTimeout = 10000;
        public boolean loggable = false;
    }


    public boolean refreshDeviceCache(String macAdress) {
        if (getBluetoothGatt(macAdress) != null) {
            try {
                BluetoothGatt localBluetoothGatt = getBluetoothGatt(macAdress);
                Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
                if (localMethod != null) {
                    boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                    return bool;
                }
            } catch (Exception localException) {
            }
        }
        return false;
    }
}
