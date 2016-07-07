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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class LinkingNotifyRange {
    private static final String TAG = "LinkingPlugIn";

    private final List<LinkingDeviceManager.OnRangeListener> mOnRangeListeners = new CopyOnWriteArrayList<>();
    private final List<LinkingDevice> mRangeDeviceHolders = new CopyOnWriteArrayList<>();

    private NotifyRange mNotifyRange;
    private Context mContext;

    public LinkingNotifyRange(final Context context) {
        mContext = context;
    }

    public synchronized void add(final LinkingDevice device) {
        if (mRangeDeviceHolders.contains(device)) {
            return;
        }
        mRangeDeviceHolders.add(device);
        startNotifyRange();
    }

    public synchronized void remove(final LinkingDevice device) {
        mRangeDeviceHolders.remove(device);

        if (mRangeDeviceHolders.isEmpty()) {
            release();
        }
    }

    public synchronized void release() {
        mOnRangeListeners.clear();
        mRangeDeviceHolders.clear();

        if (mNotifyRange != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a range event.");
            }
            mNotifyRange.release();
            mNotifyRange = null;
        }
    }

    public void addListener(final LinkingDeviceManager.OnRangeListener listener) {
        mOnRangeListeners.add(listener);
    }

    public void removeListener(final LinkingDeviceManager.OnRangeListener listener) {
        mOnRangeListeners.remove(listener);
    }

    private LinkingDevice findDeviceFromRangeHolders(final String address) {
        for (LinkingDevice device : mRangeDeviceHolders) {
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
        mNotifyRange = new NotifyRange(mContext, new NotifyRange.RangeInterface() {
            @Override
            public void onRangeChange() {
                if (mRangeDeviceHolders.isEmpty()) {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "mRangeDeviceHolders is empty.");
                    }
                    return;
                }

                SharedPreferences preference = mContext.getSharedPreferences(Define.RangeInfo, Context.MODE_PRIVATE);
                String bdAddress = preference.getString(LinkingUtil.BD_ADDRESS, "");
                int range = preference.getInt(LinkingUtil.RANGE, -1);
                int rangeSetting = preference.getInt(LinkingUtil.RANGE_SETTING, -1);
                LinkingDevice device = findDeviceFromRangeHolders(bdAddress);
                if (device != null) {
                    notifyOnChangeRange(device, LinkingDeviceManager.Range.valueOf(rangeSetting, range));
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not found a device.");
                    }
                }
            }
        });
    }

    private void notifyOnChangeRange(final LinkingDevice device, final LinkingDeviceManager.Range range) {
        for (LinkingDeviceManager.OnRangeListener listener : mOnRangeListeners) {
            listener.onChangeRange(device, range);
        }
    }
}
