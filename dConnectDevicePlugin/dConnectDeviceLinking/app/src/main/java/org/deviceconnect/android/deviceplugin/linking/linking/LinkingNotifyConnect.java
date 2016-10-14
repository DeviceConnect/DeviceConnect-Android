/*
 LinkingNotifyConnect.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.Define;
import com.nttdocomo.android.sdaiflib.NotifyConnect;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class LinkingNotifyConnect {
    private static final String TAG = "LinkingPlugIn";

    private final List<LinkingDeviceManager.OnConnectListener> mOnConnectListeners = new CopyOnWriteArrayList<>();

    private NotifyConnect mNotifyConnect;
    private LinkingDeviceManager mLinkingDeviceManager;
    private Context mContext;

    public LinkingNotifyConnect(final Context context, final LinkingDeviceManager manager) {
        mContext = context;
        mLinkingDeviceManager = manager;
        startNotifyConnect();
    }

    public void release() {
        mOnConnectListeners.clear();
        stopNotifyConnect();
    }

    public void addListener(final LinkingDeviceManager.OnConnectListener listener) {
        mOnConnectListeners.add(listener);
    }

    public void removeListener(final LinkingDeviceManager.OnConnectListener listener) {
        mOnConnectListeners.remove(listener);
    }

    private synchronized void startNotifyConnect() {
        if (mNotifyConnect != null) {
            if (BuildConfig.DEBUG) {
                Log.w(TAG, "mNotifyConnect is already running.");
            }
            return;
        }

        mNotifyConnect = new NotifyConnect(mContext, new NotifyConnect.ConnectInterface() {
            @Override
            public void onConnect() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.ConnectInfo, Context.MODE_PRIVATE);
                int deviceId = preference.getInt(LinkingUtil.DEVICE_ID, -1);
                int uniqueId = preference.getInt(LinkingUtil.DEVICE_UID, -1);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@ NotifyConnect#onConnect");
                    Log.d(TAG, LinkingUtil.BD_ADDRESS + "=" + preference.getString(LinkingUtil.BD_ADDRESS, ""));
                    Log.d(TAG, LinkingUtil.DEVICE_NAME + "=" + preference.getString(LinkingUtil.DEVICE_NAME, ""));
                    Log.d(TAG, LinkingUtil.RECEIVE_TIME + "=" + preference.getLong(LinkingUtil.RECEIVE_TIME, -1));
                    Log.d(TAG, LinkingUtil.CAPABILITY + "=" + preference.getInt(LinkingUtil.CAPABILITY, -1));
                    Log.d(TAG, LinkingUtil.EX_SENSOR_TYPE + "=" + preference.getInt(LinkingUtil.EX_SENSOR_TYPE, -1));
                }

                LinkingDevice device = mLinkingDeviceManager.findDeviceByDeviceId(deviceId, uniqueId);
                if (device != null) {
                    notifyConnect(device);
                }
            }

            @Override
            public void onDisconnect() {
                SharedPreferences preference = mContext.getSharedPreferences(Define.DisconnectInfo, Context.MODE_PRIVATE);
                int deviceId = preference.getInt(LinkingUtil.DEVICE_ID, -1);
                int uniqueId = preference.getInt(LinkingUtil.DEVICE_UID, -1);

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "@@ NotifyConnect#onDisconnect");
                    Log.d(TAG, LinkingUtil.DEVICE_NAME + "=" + preference.getString(LinkingUtil.DEVICE_NAME, ""));
                    Log.d(TAG, LinkingUtil.RECEIVE_TIME + "=" + preference.getLong(LinkingUtil.RECEIVE_TIME, -1));
                }

                LinkingDevice device = mLinkingDeviceManager.findDeviceByDeviceId(deviceId, uniqueId);
                if (device != null) {
                    device.setIsConnected(false);
                    notifyDisconnect(device);
                }
            }
        });
    }

    private synchronized void stopNotifyConnect() {
        if (mNotifyConnect != null) {
            mNotifyConnect.release();
            mNotifyConnect = null;
        }
    }

    private void notifyConnect(final LinkingDevice device) {
        for (LinkingDeviceManager.OnConnectListener listener : mOnConnectListeners) {
            listener.onConnect(device);
        }
    }

    private void notifyDisconnect(final LinkingDevice device) {
        for (LinkingDeviceManager.OnConnectListener listener : mOnConnectListeners) {
            listener.onDisconnect(device);
        }
    }
}
