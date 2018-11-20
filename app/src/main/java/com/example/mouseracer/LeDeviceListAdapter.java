package com.example.mouseracer;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mouseracer.myBle.ble.BleDevice;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by LiuLei on 2016/11/26.
 */


public class LeDeviceListAdapter extends BaseAdapter {
    private ArrayList<BleDevice> mLeDevices;
    private LayoutInflater mInflator;

    private DeviceSelectListenser deviceSelectListenser;

    public interface DeviceSelectListenser {
        void onSelectDevice(BleDevice bleDevice);
    }


    public LeDeviceListAdapter(Activity context, DeviceSelectListenser deviceSelectListenser) {
        super();
        this.deviceSelectListenser = deviceSelectListenser;
        mLeDevices = new ArrayList<BleDevice>();
        mInflator = context.getLayoutInflater();
    }

    public void addDevice(BleDevice device) {
        for (BleDevice d : mLeDevices) {
            if (d.getBleAddress().equals(device.getBleAddress())) {
                return;
            }
        }
        mLeDevices.add(device);
//        if (!mLeDevices.contains(device)) {
//            mLeDevices.add(device);
//        }
    }


    public void addDevices(List<BleDevice> devices) {
        for (BleDevice device : devices) {
            if (!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }
    }

    public BleDevice getDevice(int position) {
        return mLeDevices.get(position);
    }

    public void clear() {
        mLeDevices.clear();
    }

    @Override
    public int getCount() {
        return mLeDevices.size();
    }

    @Override
    public Object getItem(int i) {
        return mLeDevices.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (view == null) {
            view = mInflator.inflate(R.layout.listitem_device, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
            viewHolder.tvConnect = (TextView) view.findViewById(R.id.tvConnect);
            viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
            viewHolder.deviceRSSI = (TextView) view.findViewById(R.id.device_RSSI);
            viewHolder.deviceState = (TextView) view.findViewById(R.id.state);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        final BleDevice device = mLeDevices.get(i);
        final String deviceName = device.getBleName();
        final String deviceRSSI = BluetoothDevice.EXTRA_RSSI;
        if (device.isConnectting()) {
            viewHolder.deviceState.setText("正在连接中...");
        } else if (device.isConnected()) {
            viewHolder.deviceState.setText("已连接");
        } else {
            viewHolder.deviceState.setText("未连接");
        }
        if (deviceName != null && deviceName.length() > 0) {
            viewHolder.deviceName.setText(deviceName);
        } else if (deviceRSSI != null && deviceRSSI.length() > 0) {
            viewHolder.deviceRSSI.setText(deviceRSSI);
        } else {
            viewHolder.deviceName.setText(R.string.unknown_device);
        }
        viewHolder.deviceAddress.setText(device.getBleAddress());
//        viewHolder.tvConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (deviceSelectListenser != null) {
//                    deviceSelectListenser.onSelectDevice(mLeDevices.get(i));
//                }
//            }
//        });
        return view;
    }

    class ViewHolder {
        TextView deviceName;
        TextView tvConnect;
        TextView deviceAddress;
        TextView deviceRSSI;
        TextView deviceState;
    }

}
