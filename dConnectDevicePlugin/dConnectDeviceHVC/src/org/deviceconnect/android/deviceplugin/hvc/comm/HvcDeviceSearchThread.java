/*
 HvcDeviceSearchThread.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.List;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;

import omron.HVC.BleDeviceSearch;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * HVC Device search thread.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDeviceSearchThread extends Thread {
    
    /**
     * Context.
     */
    private Context mContext;
    /**
     * device search listener.
     */
    private HvcDeviceSearchListener mListener;
    
    /**
     * Constructor.
     * @param context Context
     * @param listener listener
     */
    public HvcDeviceSearchThread(final Context context, final HvcDeviceSearchListener listener) {
    	super();
    	mContext = context;
    	mListener = listener;
    }
    
    @Override
    public void run() {
        List<BluetoothDevice> devices = selectHVCDevice(HvcConstants.HVC_DEVICE_NAME_PREFIX);
        if (devices != null) {
            mListener.onDeviceSearchFinish(devices);
        } else {
            mListener.onDeviceSearchTimeout();
        }
    }
    
    /**
     * search device.
     * @param regStr regstr
     * @return found devices.
     */
    private List<BluetoothDevice> selectHVCDevice(final String regStr) {
        BleDeviceSearch bleSearch = new BleDeviceSearch(mContext);
        List<BluetoothDevice> deviceList = bleSearch.getDevices();
        if (deviceList != null && deviceList.size() > 0) {
            return deviceList;
        }
        return null;
    }

    /**
     * thread halt process.
     */
    public void halt() {
        interrupt();
    }
}

