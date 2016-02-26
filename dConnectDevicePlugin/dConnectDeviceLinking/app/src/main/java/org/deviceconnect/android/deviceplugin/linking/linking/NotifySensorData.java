package org.deviceconnect.android.deviceplugin.linking.linking;


import android.content.Context;
import android.content.IntentFilter;

import com.nttdocomo.android.sdaiflib.ControlSensorData;
import com.nttdocomo.android.sdaiflib.ReceiveSensorData;

public class NotifySensorData {

    private Context mContext;
    private ReceiveSensorData mReceiver;

    public NotifySensorData(Context context, ControlSensorData.SensorDataInterface observer) {
        this.mContext = context;
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.nttdocomo.android.smartdeviceagent.action.SENSOR_DATA");
        filter.addAction("com.nttdocomo.android.smartdeviceagent.action.STOP_SENSOR");
        this.mReceiver = new ReceiveSensorData(observer);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    public void release() {
        if (this.mContext != null && this.mReceiver != null) {
            try {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            } catch (IllegalArgumentException var2) {

            }
        }
    }


}
