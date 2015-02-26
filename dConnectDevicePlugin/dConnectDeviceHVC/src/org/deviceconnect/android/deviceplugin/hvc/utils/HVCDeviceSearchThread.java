package org.deviceconnect.android.deviceplugin.hvc.utils;

import java.util.List;

import omron.HVC.BleDeviceSearch;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

public class HVCDeviceSearchThread extends Thread {
	
	private Context mContext;
	private HVCDeviceSearchListener mListener;
	
    public HVCDeviceSearchThread(Context context, HVCDeviceSearchListener listener) {
		super();
		mContext = context;
		mListener = listener;
	}
	
	@Override
	public void run() {
		List<BluetoothDevice> devices = SelectHVCDevice("OMRON_HVC.*|omron_hvc.*");
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

