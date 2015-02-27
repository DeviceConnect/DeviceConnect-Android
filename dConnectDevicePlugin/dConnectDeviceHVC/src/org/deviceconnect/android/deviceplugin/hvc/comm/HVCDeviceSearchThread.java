package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.List;

import org.deviceconnect.android.deviceplugin.hvc.profile.HvcConstants;

import omron.HVC.BleDeviceSearch;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

public class HvcDeviceSearchThread extends Thread {
    
    private Context mContext;
    private HvcDeviceSearchListener mListener;
    
    public HvcDeviceSearchThread(Context context, HvcDeviceSearchListener listener) {
    	super();
    	mContext = context;
    	mListener = listener;
    }
    
    @Override
    public void run() {
        List<BluetoothDevice> devices = SelectHVCDevice(HvcConstants.HVC_DEVICE_NAME_PREFIX);
        if (devices != null) {
            mListener.onDeviceSearchFinish(devices);
        } else {
            mListener.onDeviceSearchTimeout();
        }
    }
    
    private List<BluetoothDevice> SelectHVCDevice(String regStr) {
        BleDeviceSearch bleSearch = new BleDeviceSearch(/*getApplicationContext()*/mContext);
        List<BluetoothDevice> deviceList = bleSearch.getDevices();
        if (deviceList != null && deviceList.size() > 0) {
            return deviceList;
        }
        return null;
    }
}

