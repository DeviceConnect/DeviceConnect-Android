/*
 HvcCommManagerUtils.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothDevice;

/**
 * HVC comm manager utility.
 * 
 * @author NTT DOCOMO, INC.
 */
public final class HvcCommManagerUtils {

    /**
     * Constructor.
     */
    private HvcCommManagerUtils() {
        
    }
    
    /**
     * search comm manager by serviceId.
     * @param commManagerArray array.
     * @param serviceId serviceId
     * @return not null : commManager/ null : not found
     */
    public static HvcCommManager search(final List<HvcCommManager> commManagerArray, final String serviceId) {
        
        for (HvcCommManager commManager : commManagerArray) {
            if (serviceId.equals(commManager.getServiceId())) {
                return commManager;
            }
        }
        
        return null;
    }

    /**
     * check exist event by interval.
     * @param commManagerArray array.
     * @param interval interval.
     * @return true: exist / false: not exist
     */
    public static boolean checkExistEventByInterval(final List<HvcCommManager> commManagerArray, final long interval) {
        
        for (HvcCommManager commManager : commManagerArray) {
            if (commManager.checkExistEventByInterval(interval)) {
                return true;
            }
        }
        return false;
    }

    /**
     * get connected bluetooth devices.
     * @param commManagerArray comm manager array
     * @return connected bluetooth device array
     */
    public static List<BluetoothDevice> getConnectedBluetoothDevices(final List<HvcCommManager> commManagerArray) {
        List<BluetoothDevice> deviceList = new ArrayList<>();
        for (HvcCommManager commManager : commManagerArray) {
            if (commManager.isConnected()) {
                deviceList.add(commManager.getBluetoothDevice());
            }
        }
        return deviceList;
    }

}
