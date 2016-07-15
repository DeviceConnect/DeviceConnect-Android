/*
 LinkingNotifyRange.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.NotifyRange;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

class LinkingNotifyRange {
    private static final String TAG = "LinkingPlugIn";

    private final Map<LinkingDevice, List<LinkingDeviceManager.OnRangeListener>> mMap = new HashMap<>();

    private NotifyRange mNotifyRange;
    private Context mContext;

    public LinkingNotifyRange(final Context context) {
        mContext = context;
    }

    public synchronized void enableListenRange(final LinkingDevice device, final LinkingDeviceManager.OnRangeListener listener) {
        List<LinkingDeviceManager.OnRangeListener> listeners = mMap.get(device);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            mMap.put(device, listeners);
        } else if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);

        startNotifyRange();
    }

    public synchronized void disableListenRange(final LinkingDevice device, final LinkingDeviceManager.OnRangeListener listener) {
        List<LinkingDeviceManager.OnRangeListener> listeners = mMap.get(device);
        if (listeners != null) {
            if (listener == null) {
                mMap.remove(device);
            } else {
                listeners.remove(listener);
                if (listeners.isEmpty()) {
                    mMap.remove(device);
                }
            }
        }

        if (mMap.isEmpty()) {
            stopNotifyRange();
        }
    }

    public synchronized void release() {
        mMap.clear();
        stopNotifyRange();
    }

    private synchronized LinkingDevice findDeviceFromRangeHolders(final String address) {
        for (LinkingDevice device : mMap.keySet()) {
            if (device.getBdAddress().equals(address)) {
                return device;
            }
        }
        return null;
    }

    private void startNotifyRange() {
        if (mNotifyRange != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyRange is already running.");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startNotifyRange");
        }

        mNotifyRange = new NotifyRange(mContext, new NotifyRange.RangeInterface() {
            @Override
            public void onRangeChange() {
                if (mMap.isEmpty()) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "mMap is empty.");
                    }
                    return;
                }

                SharedPreferences preference = mContext.getSharedPreferences(Define.RangeInfo, Context.MODE_PRIVATE);
                String bdAddress = preference.getString(LinkingUtil.BD_ADDRESS, "");
                int range = preference.getInt(LinkingUtil.RANGE, -1);
                int rangeSetting = preference.getInt(LinkingUtil.RANGE_SETTING, -1);
                LinkingDevice device = findDeviceFromRangeHolders(bdAddress);
                if (device != null) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "NotifyRange: [" + device.getDisplayName() + "] " + range);
                    }
                    notifyOnChangeRange(device, LinkingDeviceManager.Range.valueOf(rangeSetting, range));
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not found a device.");
                    }
                }
            }
        });
    }

    private void stopNotifyRange() {
        if (mNotifyRange != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a range event.");
            }
            mNotifyRange.release();
            mNotifyRange = null;
        }
    }

    private synchronized void notifyOnChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
        for (LinkingDeviceManager.OnRangeListener listener : mMap.get(device)) {
            listener.onChangeRange(device, range);
        }
    }
}
