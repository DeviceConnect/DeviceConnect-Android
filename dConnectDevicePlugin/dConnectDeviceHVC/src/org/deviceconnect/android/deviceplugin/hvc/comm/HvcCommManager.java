package org.deviceconnect.android.deviceplugin.hvc.comm;

import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;

import omron.HVC.HVC;
import omron.HVC.HVC_PRM;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * HVC Communication Manager.
 */
public class HvcCommManager {

    private HvcDeviceSearchThread deviceSearchThread = null;
    private HvcDetectThread detectThread = null;
    
    /**
     * Device search process result.
     */
    public enum DeviceSearchResult {
    	/**
    	 * Success.
    	 */
    	RESULT_SUCCESS,
    	/**
    	 * ERROR Thread alived.
    	 */
    	RESULT_ERR_THREAD_ALIVE,
    	
    };
    
    /**
     * Start device search thread. 
     * @param context Context
     * @param listener callback listener.
     * @return result
     */
    public DeviceSearchResult startDeviceSearchThread(final Context context, final HvcDeviceSearchListener listener) {
        if (deviceSearchThread == null || !deviceSearchThread.isAlive()) {
            deviceSearchThread = new HvcDeviceSearchThread(context, listener); 
            deviceSearchThread.start();
            return DeviceSearchResult.RESULT_SUCCESS;
        } else {
            return DeviceSearchResult.RESULT_ERR_THREAD_ALIVE;
        }
    }
    
    /**
     * Detection process result.
     */
    public enum DetectionResult {
        /**
         * Success.
         */
        RESULT_SUCCESS,
        /**
         * ERROR serviceId not found.
         */
        RESULT_ERR_SERVICEID_NOT_FOUND,
        /**
         * ERROR Thread alived.
         */
        RESULT_ERR_THREAD_ALIVE,
    };
    
    /**
     * Start detect face thread. 
     * @param context Context
     * @param device device
     * @param useFunc HVC useFunc
     * @param params HVC parameter.
     * @param listener listener
     * @return result
     */
    public DetectionResult startDetectThread(final Context context, final BluetoothDevice device, final int useFunc,
            final HvcDetectRequestParams params, final HvcDetectListener listener) {
       if (detectThread == null || !detectThread.isAlive()) {
            detectThread = new HvcDetectThread(context, device, useFunc, params, listener);
            detectThread.start();
            return DetectionResult.RESULT_SUCCESS;
        } else {
            return DetectionResult.RESULT_ERR_THREAD_ALIVE;
        }
    }
}
