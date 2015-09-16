package org.deviceconnect.android.deviceplugin.irkit;

import android.app.Application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class IRKitApplication extends Application {
    /**
     * 検知したデバイス群.
     */
    private List<IRKitDevice> mDevices;

    /**
     * IRKitのListViewの位置.
     */
    /**
     * IRKiのデバイスを保持する.
     */
    public void setIRKitDevices(final ConcurrentHashMap<String, IRKitDevice> devices) {
        if (mDevices != null) {
            mDevices.clear();
            mDevices = null;
        }
        mDevices = new ArrayList<IRKitDevice>();
        for (Map.Entry<String, IRKitDevice> device : devices.entrySet()) {
            mDevices.add(device.getValue());
        }

    }

    /**
     * IRKiのデバイスを返す.
     * @return 検知したデバイス群
     */
    public List<IRKitDevice> getIRKitDevices() {
        return mDevices;
    }
}
