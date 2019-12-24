/*
 BleDeviceDetector
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;
import org.deviceconnect.android.deviceplugin.hvc.ble.adapter.NewBleDeviceAdapterImpl;
import org.deviceconnect.android.deviceplugin.hvc.ble.adapter.OldBleDeviceAdapterImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This class to detect the BLE device.
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
     * Stops scanning after 2 second.
     */
    private static final long SCAN_PERIOD = 2000;

    /**
     * Context.
     */
    private Context mContext;
    
    /**
     * Ble adapter.
     */
    private BleDeviceAdapter mBleAdapter;

    /**
     * Handler.
     */
    private Handler mHandler;
    
    /**
     * Scanning flag.
     */
    private boolean mScanning;
    
    /**
     * Ble device disconvery listener.
     */
    private BleDeviceDiscoveryListener mListener;
    /**
     * devices.
     */
    private List<BluetoothDevice> mDevices = new CopyOnWriteArrayList<>();

    /**
     * Factory.
     */
    private BleDeviceAdapterFactory mFactory;

    /**
     * Constructor.
     *
     * @param context context of this application
     */
    public BleDeviceDetector(final Context context) {
        this(context, new BleDeviceAdapterFactory() {
            @Override
            public BleDeviceAdapter createAdapter(final Context context) {
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
     *
     * @param context context of this application
     * @param factory factory class
     */
    public BleDeviceDetector(final Context context, final BleDeviceAdapterFactory factory) {
        mContext = context;
        mFactory = factory;
        initialize();
    }

    /**
     * Get a context.
     *
     * @return context
     */
    public Context getContext() {
        return mContext;
    }

    /**
     * Initialize the BleDeviceDetector.
     */
    public synchronized void initialize() {
        if (mBleAdapter == null) {
            mBleAdapter = mFactory.createAdapter(mContext);
        }
    }

    /**
     * Get a state of scan.
     *
     * @return true if scanning a BLE
     */
    public boolean isScanning() {
        return mScanning;
    }

    /**
     * Return true if Bluetooth is currently enabled and ready for use.
     *
     * @return true if the local adapter is turned on
     */
    public boolean isEnabled() {
        if (mBleAdapter == null) {
            return false;
        }

        if (!BleUtils.isBLEPermission(getContext())) {
            return false;
        }

        return mBleAdapter.isEnabled();
    }

    /**
     * Sets a BluetoothDiscoveryListener.
     *
     * @param listener Notify that discovered the bluetooth device
     */
    public void setListener(final BleDeviceDiscoveryListener listener) {
        mListener = listener;
    }

    /**
     * Start Bluetooth LE scan.
     */
    public void startScan() {
        if (isEnabled()) {
            scanLeDevice(true);
        }
    }

    /**
     * Stop Bluetooth LE scan.
     */
    public void stopScan() {
        if (isEnabled()) {
            scanLeDevice(false);
        }
    }

    /**
     * Gets a BluetoothDevice from address.
     *
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
     * Remove a BluetoothDevice from device name.
     * @param name bluetooth device name
     */
    public void removeCacheDevice(final String name) {
        for (int i = 0; i < mDevices.size(); i++) {
            if (mDevices.get(i) != null && mDevices.get(i).getName().equals(name)) {
                mDevices.remove(i);
            }
        }
    }
    /**
     * Gets the set of BluetoothDevice that are bonded (paired) to the local adapter.
     *
     * @return set of BluetoothDevice, or null on error
     */
    public Set<BluetoothDevice> getBondedDevices() {
        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            return null;
        }
        return mBleAdapter.getBondedDevices();
    }

    /**
     * Check bluetooth address.
     * @param address address
     * @return check result. 
     */
    public boolean checkBluetoothAddress(final String address) {
        return mBleAdapter.checkBluetoothAddress(address);
    }

    /**
     * Scan a BLE only once.
     * @param listener listener
     */
    public synchronized void scanLeDeviceOnce(final BleDeviceDiscoveryListener listener) {
        final List<BluetoothDevice> devices = new ArrayList<>();
        final BleDeviceAdapter.BleDeviceScanCallback callback =
                new BleDeviceAdapter.BleDeviceScanCallback() {
                    @Override
                    public void onLeScan(final BluetoothDevice device, final int rssi) {
                        devices.add(device);
                    }

                    @Override
                    public void onFail() {

                    }
                };
        
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mBleAdapter.isEnabled()) {
                    mBleAdapter.stopScan(callback);
                    listener.onDiscovery(devices);
                }
            }
        }, SCAN_PERIOD);
        mBleAdapter.startScan(callback);
    }

    /**
     * Sets the scan state of BLE.
     *
     * @param enable Start the scan if enable is true. Stop the scan if enable is false
     */
    private synchronized void scanLeDevice(final boolean enable) {

        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            return;
        }

        if (enable) {
            
            mHandler = new Handler();
            
            if (mScanning) {
                // scan have already started.
                return;
            }
            mScanning = true;
            mScanTimerFuture = mExecutor.scheduleAtFixedRate(() -> {
                // Stops scanning after a pre-defined scan period.
                mHandler.postDelayed(() -> {
                    if (mBleAdapter.isEnabled()) {
                        stopBleScan();
                        notifyBluetoothDevice();
                    } else {
                        mScanTimerFuture.cancel(true);
                        mScanning = false;
                    }
                }, SCAN_PERIOD);
                if (mBleAdapter.isEnabled()) {
                    startBleScan();
                } else {
                    mScanTimerFuture.cancel(true);
                    mScanning = false;
                }
            }, SCAN_FIRST_WAIT_PERIOD, SCAN_WAIT_PERIOD, TimeUnit.MILLISECONDS);
        } else {
            mScanning = false;
            stopBleScan();
            if (mScanTimerFuture != null) {
                mScanTimerFuture.cancel(true);
                mScanTimerFuture = null;
            }
        }
    }

    /**
     * Starts a BLE scan.
     */
    private void startBleScan() {
        try {
            mBleAdapter.startScan(mScanCallback);
        } catch (Exception e) {
            // Exception occurred when the BLE state is invalid.
            if (BuildConfig.DEBUG) {
                Log.e("hvc.dplugin", "", e);
            }
        }
    }

    /**
     * Stops a BLE scan.
     */
    private void stopBleScan() {
        try {
            mBleAdapter.stopScan(mScanCallback);
        } catch (Exception e) {
            // Exception occurred when the BLE state is invalid.
            if (BuildConfig.DEBUG) {
                Log.e("hvc.dplugin", "", e);
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
     * Implement the BluetoothAdapter.
     */
    private final BleDeviceAdapter.BleDeviceScanCallback mScanCallback =
            new BleDeviceAdapter.BleDeviceScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi) {
                    if (!mDevices.contains(device)) {
                        mDevices.add(device);
                    }
                }

                @Override
                public void onFail() {

                }
            };

    /**
     * This listener to be notified when discovered the BLE device.
     */
    public interface BleDeviceDiscoveryListener {
        /**
         * Discovered the BLE device.
         *
         * @param devices BLE device list
         */
        void onDiscovery(List<BluetoothDevice> devices);
    }
}
