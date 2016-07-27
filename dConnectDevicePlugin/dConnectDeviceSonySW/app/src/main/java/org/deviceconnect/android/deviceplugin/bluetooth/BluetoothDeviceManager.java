package org.deviceconnect.android.deviceplugin.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class BluetoothDeviceManager {

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            info("Action: " + action);

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                if (filter(device) && device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    if (!isCached(device)) {
                        cache(device);
                        notifyOnFound(device);
                    }
                    notifyOnConnected(device);
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                if (filter(device)) {
                    if (!isCached(device)) {
                        cache(device);
                        notifyOnFound(device);
                    }
                    notifyOnDisconnected(device);
                }
            }
        }
    };

    private final Context mContext;

    private final Map<String, BluetoothDevice> mDeviceCache
        = new HashMap<String, BluetoothDevice>();

    private final List<DeviceListener> mDeviceListeners = new ArrayList<DeviceListener>();

    private DeviceFilter mDeviceFilter;

    private Logger mLogger;

    public BluetoothDeviceManager(final Context context, final DeviceFilter filter) {
        mContext = context;
        mDeviceFilter = filter;

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            for (BluetoothDevice device : adapter.getBondedDevices()) {
                if (filter(device)) {
                    cache(device);
                }
            }
        }
    }

    public void setLogger(final Logger logger) {
        mLogger = logger;
    }

    public List<BluetoothDevice> getCachedDeviceList() {
        return new ArrayList<BluetoothDevice>(mDeviceCache.values());
    }

    public void start() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void stop() {
        mContext.unregisterReceiver(mReceiver);
    }

    public void addDeviceListener(final DeviceListener listener) {
        synchronized (mDeviceListeners) {
            if (!mDeviceListeners.contains(listener)) {
                mDeviceListeners.add(listener);
            }
        }
    }

    public void removeDeviceListener(final DeviceListener listener) {
        synchronized (mDeviceListeners) {
            mDeviceListeners.remove(listener);
        }
    }

    private void notifyOnFound(final BluetoothDevice device) {
        synchronized (mDeviceListeners) {
            for (DeviceListener l : mDeviceListeners) {
                l.onFound(device);
            }
        }
    }

    private void notifyOnConnected(final BluetoothDevice device) {
        synchronized (mDeviceListeners) {
            for (DeviceListener l : mDeviceListeners) {
                l.onConnected(device);
            }
        }
    }

    private void notifyOnDisconnected(final BluetoothDevice device) {
        synchronized (mDeviceListeners) {
            for (DeviceListener l : mDeviceListeners) {
                l.onDisconnected(device);
            }
        }
    }

    private boolean filter(final BluetoothDevice device) {
        if (mDeviceFilter == null) {
            return true;
        }
        return mDeviceFilter.filter(device);
    }

    private void info(final String message) {
        if (mLogger != null) {
            mLogger.info(message);
        }
    }

    private boolean isCached(final BluetoothDevice device) {
        return mDeviceCache.get(getDeviceId(device)) != null;
    }

    private void cache(final BluetoothDevice device) {
        mDeviceCache.put(getDeviceId(device), device);
    }

    private String getDeviceId(final BluetoothDevice device) {
        return device.getAddress();
    }

    public interface DeviceListener {

        void onFound(BluetoothDevice device);

        void onConnected(BluetoothDevice device);

        void onDisconnected(BluetoothDevice device);

    }

    public interface DeviceFilter {

        boolean filter(BluetoothDevice device);

    }
}
