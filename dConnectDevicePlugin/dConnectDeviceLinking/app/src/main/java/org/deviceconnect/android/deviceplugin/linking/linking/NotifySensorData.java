/*
 NotifySensorData.java
 Copyright (c) 2016 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.linking.linking;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import com.nttdocomo.android.sdaiflib.ControlSensorData;
import com.nttdocomo.android.sdaiflib.ReceiveSensorData;

import org.deviceconnect.android.deviceplugin.linking.BuildConfig;

public class NotifySensorData {

    private static final String TAG = "LinkingPlugIn";

    private Context mContext;
    private ReceiveSensorData mReceiver;

    public NotifySensorData(final Context context, final ControlSensorData.SensorDataInterface observer) {
        mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nttdocomo.android.smartdeviceagent.action.SENSOR_DATA");
        filter.addAction("com.nttdocomo.android.smartdeviceagent.action.STOP_SENSOR");
        mReceiver = new ReceiveSensorData(observer);
        mContext.registerReceiver(mReceiver, filter);
    }

    public void release() {
        if (mContext != null && mReceiver != null) {
            try {
                mContext.unregisterReceiver(mReceiver);
                mReceiver = null;
            } catch (IllegalArgumentException e) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "", e);
                }
            }
        }
    }
}
