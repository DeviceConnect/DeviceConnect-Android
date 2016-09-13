/*
 LinkingNotifyKey.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.NotifyNotification;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class LinkingNotifyKey {
    private static final String TAG = "LinkingPlugIn";

    private final List<LinkingDeviceManager.OnKeyEventListener> mOnKeyEventListeners = new CopyOnWriteArrayList<>();
    private final List<LinkingDevice> mKeyEventDeviceHolders = new CopyOnWriteArrayList<>();

    private NotifyNotification mNotifyNotification;
    private Context mContext;

    public LinkingNotifyKey(final Context context) {
        mContext = context;
    }

    public synchronized void add(final LinkingDevice device) {
        if (mKeyEventDeviceHolders.contains(device)) {
            return;
        }
        mKeyEventDeviceHolders.add(device);
        start();
    }

    public synchronized void remove(final LinkingDevice device) {
        mKeyEventDeviceHolders.remove(device);

        if (mKeyEventDeviceHolders.isEmpty()) {
            release();
        }
    }

    public synchronized void release() {
        mOnKeyEventListeners.clear();
        mKeyEventDeviceHolders.clear();

        if (mNotifyNotification != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a key event.");
            }
            mNotifyNotification.release();
            mNotifyNotification = null;
        }
    }

    public void addListener(final LinkingDeviceManager.OnKeyEventListener listener) {
        mOnKeyEventListeners.add(listener);
    }

    public void removeListener(final LinkingDeviceManager.OnKeyEventListener listener) {
        mOnKeyEventListeners.remove(listener);
    }

    private LinkingDevice findDeviceFromKeyHolders(final int deviceId, final int uniqueId) {
        for (LinkingDevice device : mKeyEventDeviceHolders) {
            if (device.getModelId() == deviceId && device.getUniqueId() == uniqueId) {
                return device;
            }
        }
        return null;
    }

    private void start() {
        if (mNotifyNotification != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyNotification is already running.");
            }
            return;
        }
        mNotifyNotification = new NotifyNotification(mContext, new NotifyNotification.NotificationInterface() {
            @Override
            public void onNotify() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.NotificationInfo, Context.MODE_PRIVATE);
                int deviceId = preference.getInt(LinkingUtil.DEVICE_ID, -1);
                int uniqueId = preference.getInt(LinkingUtil.DEVICE_UID, -1);
                int keyCode = preference.getInt(LinkingUtil.DEVICE_BUTTON_ID, -1);

                if (BuildConfig.DEBUG) {
                    Log.i(TAG, "NotifyNotification.NotificationInterface#onNotify");
                    Log.i(TAG, "deviceId:" + deviceId);
                    Log.i(TAG, "uniqueId:" + uniqueId);
                    Log.i(TAG, "keyCode:" + keyCode);
                }

                LinkingDevice device = findDeviceFromKeyHolders(deviceId, uniqueId);
                if (device != null) {
                    notifyOnKeyEvent(device, keyCode);
                } else {
                    if (BuildConfig.DEBUG) {
                        Log.w(TAG, "Not found a device.");
                    }
                }
            }
        });
    }

    private void notifyOnKeyEvent(final LinkingDevice device, final int keyCode) {
        for (LinkingDeviceManager.OnKeyEventListener listener : mOnKeyEventListeners) {
            listener.onKeyEvent(device, keyCode);
        }
    }
}
