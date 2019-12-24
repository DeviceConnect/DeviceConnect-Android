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

import org.deviceconnect.android.deviceplugin.linking.lib.BuildConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

class LinkingNotifyNotification {
    private static final String TAG = "LinkingPlugIn";

    private final Map<LinkingDevice, List<LinkingDeviceManager.OnButtonEventListener>> mMap = new HashMap<>();

    private NotifyNotification mNotifyNotification;
    private Context mContext;

    public LinkingNotifyNotification(final Context context) {
        mContext = context;
    }

    public synchronized void enableListenNotification(final LinkingDevice device,
                                                      final LinkingDeviceManager.OnButtonEventListener listener) {
        if (!device.isSupportButton()) {
            return;
        }

        List<LinkingDeviceManager.OnButtonEventListener> listeners = mMap.get(device);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<>();
            mMap.put(device, listeners);
        } else if (listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);

        startNotifyNotification();
    }

    public synchronized void disableListenNotification(final LinkingDevice device,
                                                       final LinkingDeviceManager.OnButtonEventListener listener) {
        List<LinkingDeviceManager.OnButtonEventListener> listeners = mMap.get(device);
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
            stopNotifyNotification();
        }
    }

    public synchronized void release() {
        mMap.clear();
        stopNotifyNotification();
    }

    private synchronized LinkingDevice findDeviceFromKeyHolders(final int deviceId, final int uniqueId) {
        for (LinkingDevice device : mMap.keySet()) {
            if (device.getModelId() == deviceId && device.getUniqueId() == uniqueId) {
                return device;
            }
        }
        return null;
    }

    private void startNotifyNotification() {
        if (mNotifyNotification != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyNotification is already running.");
            }
            return;
        }

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "startNotifyNotification");
        }

        mNotifyNotification = new NotifyNotification(mContext, () -> {
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
        });
    }

    private void stopNotifyNotification() {
        if (mNotifyNotification != null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Stop a key event.");
            }
            mNotifyNotification.release();
            mNotifyNotification = null;
        }
    }

    private synchronized void notifyOnKeyEvent(final LinkingDevice device, final int keyCode) {
        for (LinkingDeviceManager.OnButtonEventListener listener : mMap.get(device)) {
            listener.onButtonEvent(device, keyCode);
        }
    }
}
