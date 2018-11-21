/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.example.mouseracer.nordic;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.example.mouseracer.util.Constants;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

@SuppressWarnings("unused")
public class GlucoseManager extends BatteryManager<GlucoseManagerCallbacks> {
    private static final String TAG = "GlucoseManager";

    /**
     * Glucose service UUID
     */
    public final static UUID GLS_SERVICE_UUID = UUID.fromString("00001808-0000-1000-8000-00805f9b34fb");
    /**
     * Glucose Measurement characteristic UUID
     */
    private final static UUID GM_CHARACTERISTIC = UUID.fromString("00002A18-0000-1000-8000-00805f9b34fb");
    /**
     * Glucose Measurement Context characteristic UUID
     */
    private final static UUID GM_CONTEXT_CHARACTERISTIC = UUID.fromString("00002A34-0000-1000-8000-00805f9b34fb");
    /**
     * Glucose Feature characteristic UUID
     */
    private final static UUID GF_CHARACTERISTIC = UUID.fromString("00002A51-0000-1000-8000-00805f9b34fb");
    /**
     * Record Access Control Point characteristic UUID
     */
    private final static UUID RACP_CHARACTERISTIC = UUID.fromString(Constants.writeUiid);

    private BluetoothGattCharacteristic mGlucoseMeasurementCharacteristic;
    private BluetoothGattCharacteristic mGlucoseMeasurementContextCharacteristic;
    //写命令
    private BluetoothGattCharacteristic mRecordAccessControlPointCharacteristic;

    private Handler mHandler;
    private static GlucoseManager mInstance;

    /**
     * Returns the singleton implementation of GlucoseManager.
     */
    public static GlucoseManager getGlucoseManager(final Context context) {
        if (mInstance == null)
            mInstance = new GlucoseManager(context);
        return mInstance;
    }

    private GlucoseManager(final Context context) {
        super(context);
        mHandler = new Handler();
    }

    @NonNull
    @Override
    protected BatteryManagerGattCallback getGattCallback() {
        return mGattCallback;
    }

    /**
     * BluetoothGatt callbacks for connection/disconnection, service discovery,
     * receiving notification, etc.
     */
    private final BatteryManagerGattCallback mGattCallback = new BatteryManagerGattCallback() {

        @Override
        protected void initialize() {
            super.initialize();
            setNotificationCallback(mGlucoseMeasurementContextCharacteristic)
                    .with(new DataReceivedCallback() {
                        @Override
                        public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {

                        }
                    });

        }

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(GLS_SERVICE_UUID);
            if (service != null) {
                mGlucoseMeasurementCharacteristic = service.getCharacteristic(GM_CHARACTERISTIC);
                mGlucoseMeasurementContextCharacteristic = service.getCharacteristic(GM_CONTEXT_CHARACTERISTIC);
                mRecordAccessControlPointCharacteristic = service.getCharacteristic(RACP_CHARACTERISTIC);
            }
            return mGlucoseMeasurementCharacteristic != null && mRecordAccessControlPointCharacteristic != null;
        }

        @Override
        protected boolean isOptionalServiceSupported(@NonNull BluetoothGatt gatt) {
            super.isOptionalServiceSupported(gatt);
            return mGlucoseMeasurementContextCharacteristic != null;
        }

        @Override
        protected void onDeviceDisconnected() {
            mGlucoseMeasurementCharacteristic = null;
            mGlucoseMeasurementContextCharacteristic = null;
            mRecordAccessControlPointCharacteristic = null;
        }
    };

    public void writeData(byte[] bytes) {
        writeCharacteristic(mRecordAccessControlPointCharacteristic,
                bytes).enqueue();
    }


    /**
     * Clears the records list locally.
     */
    public void clear() {
        // mRecords.clear();
        mCallbacks.onOperationCompleted(getBluetoothDevice());
    }

    /**
     * Sends the request to obtain the last (most recent) record from glucose device. The data will
     * be returned to Glucose Measurement characteristic as a notification followed by Record Access
     * Control Point indication with status code Success or other in case of error.
     */
//	public void getLastRecord() {
//		if (mRecordAccessControlPointCharacteristic == null)
//			return;
//
//		clear();
//		mCallbacks.onOperationStarted(getBluetoothDevice());
//		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportLastStoredRecord())
//				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
//				.enqueue();
//	}

    /**
     * Sends the request to obtain the first (oldest) record from glucose device. The data will be
     * returned to Glucose Measurement characteristic as a notification followed by Record Access
     * Control Point indication with status code Success or other in case of error.
     */
//	public void getFirstRecord() {
//		if (mRecordAccessControlPointCharacteristic == null)
//			return;
//
//		clear();
//		mCallbacks.onOperationStarted(getBluetoothDevice());
//		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportFirstStoredRecord())
//				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
//				.enqueue();
//	}

    /**
     * Sends the request to obtain all records from glucose device. Initially we want to notify user
     * about the number of the records so the 'Report Number of Stored Records' is send. The data
     * will be returned to Glucose Measurement characteristic as a notification followed by
     * Record Access Control Point indication with status code Success or other in case of error.
     */
//	public void getAllRecords() {
//		if (mRecordAccessControlPointCharacteristic == null)
//			return;
//
//		clear();
//		mCallbacks.onOperationStarted(getBluetoothDevice());
//		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.reportNumberOfAllStoredRecords())
//				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
//				.enqueue();
//	}

    /**
     * Sends the request to obtain from the glucose device all records newer than the newest one
     * from local storage. The data will be returned to Glucose Measurement characteristic as
     * a notification followed by Record Access Control Point indication with status code Success
     * or other in case of error.
     * <p>
     * Refresh button will not download records older than the oldest in the local memory.
     * E.g. if you have pressed Last and then Refresh, than it will try to get only newer records.
     * However if there are no records, it will download all existing (using {@link #getAllRecords()}).
     */
//	public void refreshRecords() {
//		if (mRecordAccessControlPointCharacteristic == null)
//			return;
//
//		if (mRecords.size() == 0) {
//			getAllRecords();
//		} else {
//			mCallbacks.onOperationStarted(getBluetoothDevice());
//
//			// obtain the last sequence number
//			final int sequenceNumber = mRecords.keyAt(mRecords.size() - 1) + 1;
//
//			writeCharacteristic(mRecordAccessControlPointCharacteristic,
//					RecordAccessControlPointData.reportStoredRecordsGreaterThenOrEqualTo(sequenceNumber))
//					.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
//					.enqueue();
//			// Info:
//			// Operators OPERATOR_LESS_THEN_OR_EQUAL and OPERATOR_RANGE are not supported by Nordic Semiconductor Glucose Service in SDK 4.4.2.
//		}
//	}

    /**
     * Sends abort operation signal to the device.
     */
//	public void abort() {
//		if (mRecordAccessControlPointCharacteristic == null)
//			return;
//
//		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.abortOperation())
//				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
//				.enqueue();
//	}

    /**
     * Sends the request to delete all data from the device. A Record Access Control Point
     * indication with status code Success (or other in case of error) will be send.
     */
//	public void deleteAllRecords() {
//		if (mRecordAccessControlPointCharacteristic == null)
//			return;
//
//		clear();
//		mCallbacks.onOperationStarted(getBluetoothDevice());
//		writeCharacteristic(mRecordAccessControlPointCharacteristic, RecordAccessControlPointData.deleteAllStoredRecords())
//				.with((device, data) -> log(LogContract.Log.Level.APPLICATION, "\"" + RecordAccessControlPointParser.parse(data) + "\" sent"))
//				.enqueue();
//	}
}
