/*
 BleDeviceDetector
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.heartrate.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Handler;

import org.deviceconnect.android.deviceplugin.heartrate.ble.adapter.NewBleDeviceAdapterImpl;
import org.deviceconnect.android.deviceplugin.heartrate.ble.adapter.OldBleDeviceAdapterImpl;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author NTT DOCOMO, INC.
 */
public class BleDeviceDetector {
    /**
     * Instance of ScheduledExecutorService.
     */
    private ScheduledExecutorService mExecutor = Executors.newSingleThreadScheduledExecutor();

    /**
     * ScheduledFuture of scan timer.
     */
    private ScheduledFuture<?> mScanTimerFuture;

    /**
     * Defines a delay 1 second at first execution.
     */
    private static final long SCAN_FIRST_WAIT_PERIOD = 1000;

    /**
     * Defines a period 10 seconds between successive executions.
     */
    private static final long SCAN_WAIT_PERIOD = 10 * 1000;

    /**
     * Stops scanning after 1 second.
     */
    private static final long SCAN_PERIOD = 1000;

    private Context mContext;
    private BleDeviceAdapter mBleAdapter;

    private Handler mHandler = new Handler();
    private boolean mScanning;
    private BleDeviceDiscoveryListener mListener;
    private List<BluetoothDevice> mDevices = new CopyOnWriteArrayList<>();

    /**
     * Constructor.
     * @param context context of this application
     */
    public BleDeviceDetector(final Context context) {
        this(context, new BleDeviceAdapterFactory() {
            @Override
            public BleDeviceAdapter createAdapter(Context context) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    return new NewBleDeviceAdapterImpl(context);
                } else {
                    return new OldBleDeviceAdapterImpl(context);
                }
            }
        });
    }

    /**
     * Constructor.
     * @param context context of this application
     * @param factory factory class
     */
    public BleDeviceDetector(final Context context, final BleDeviceAdapterFactory factory) {
        mContext = context;
        mBleAdapter = factory.createAdapter(context);
    }

    /**
     * Get a context.
     * @return context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Get a state of scan.
     * @return true if scanning a ble
     */
    public boolean isScanning() {
        return mScanning;
    }

    public boolean isEnabled() {
        if (mBleAdapter == null) {
            return false;
        }
        return mBleAdapter.isEnabled();
    }

    /**
     * Sets a BluetoothDiscoveryListener.
     * @param listener Notify that discovered the bluetooth device
     */
    public void setListener(final BleDeviceDiscoveryListener listener) {
        mListener = listener;
    }

    /**
     * Start Bluetooth LE scan.
     */
    public void startScan() {
        scanLeDevice(true);
    }

    /**
     * Stop Bluetooth LE scan.
     */
    public void stopScan() {
        scanLeDevice(false);
    }

    /**
     * Gets a BluetoothDevice from address.
     * @param address bluetooth address
     * @return instance of BluetoothDevice, null if not found device
     */
    public BluetoothDevice getDevice(final String address) {
        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            return null;
        }
        return mBleAdapter.getDevice(address);
    }

    /**
     * Sets the scan state of BLE.
     * @param enable Start the scan if enable is true. Stop the scan if enable is false
     */
    private synchronized void scanLeDevice(final boolean enable) {

        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            return;
        }

        if (enable) {
            mScanning = true;
            mScanTimerFuture = mExecutor.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    // Stops scanning after a pre-defined scan period.
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBleAdapter.stopScan(mScanCallback);
                            notifyBluetoothDevice();
                        }
                    }, SCAN_PERIOD);
                    mDevices.clear();
                    mBleAdapter.startScan(mScanCallback);
                }
            }, SCAN_FIRST_WAIT_PERIOD, SCAN_WAIT_PERIOD, TimeUnit.MILLISECONDS);
        } else {
            mScanning = false;
            mBleAdapter.stopScan(mScanCallback);
            if (mScanTimerFuture != null) {
                mScanTimerFuture.cancel(true);
            }
        }
    }

    /**
     * Notify that all bluetooth device was discovered.
     */
    private void notifyBluetoothDevice() {
        if (mListener != null) {
            mListener.onDiscovery(mDevices);
        }
    }

    /**
     * Bluetooth Adapter.
     */
    private final BleDeviceAdapter.BleDeviceScanCallback mScanCallback =
            new BleDeviceAdapter.BleDeviceScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi) {
            if (!mDevices.contains(device)) {
                mDevices.add(device);
            }
        }
    };

    /**
     * This listener to be notified when discovered the ble device.
     */
    public static interface BleDeviceDiscoveryListener {
        /**
         * Discovered the ble device.
         * @param devices ble device list
         */
        void onDiscovery(List<BluetoothDevice> devices);
    }
}
