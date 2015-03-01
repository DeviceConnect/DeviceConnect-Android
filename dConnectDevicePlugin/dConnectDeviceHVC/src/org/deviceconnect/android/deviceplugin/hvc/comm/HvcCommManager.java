package org.deviceconnect.android.deviceplugin.hvc.comm;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectEvent;
import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectKind;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;

import android.bluetooth.BluetoothDevice;
import android.content.Context;


/**
 * HVC Communication Manager.
 */
public class HvcCommManager {
    
    /**
     * ServiceId.
     */
    private String mServiceId;
    
    /**
     * Device search thread.
     */
    private HvcDeviceSearchThread mDeviceSearchThread;
    
    /**
     * Detect thread..
     */
    private HvcDetectThread mDetectThread;
    
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
     * Constructor.
     * @param serviceId serviceId
     */
    public HvcCommManager(final String serviceId) {
        mServiceId = serviceId;
    }
    
    /**
     * get serviceId.
     * @return serviceId.
     */
    public String getServiceId() {
        return mServiceId;
    }
    
    
    
    //
    // Register / Unregister Event.
    //
    
    /**
     * event array.
     */
    private List<HumanDetectEvent> mEventArray = new ArrayList<HumanDetectEvent>();

    /**
     * register detect event.
     * @param detectKind detectKind
     * @param sessionKey sessionKey
     * @param options options
     */
    public void registerDetectEvent(final HumanDetectKind detectKind, final String sessionKey,
            final List<String> options) {
        HumanDetectEvent event = new HumanDetectEvent(detectKind, sessionKey, options);
        mEventArray.add(event);
    }
    
    /**
     * unregister detect event.
     * @param detectKind detectKind
     * @param sessionKey sessionKey
     */
    public void unregisterDetectEvent(final HumanDetectKind detectKind, final String sessionKey) {
        int count = mEventArray.size();
        for (int index = (count - 1); index >= 0; index--) {
            HumanDetectEvent event = mEventArray.get(index);
            if (detectKind == event.getKind() && sessionKey.equals(event.getSessionKey())) {
                mEventArray.remove(index);
            }
        }
    }
    
    /**
     * check register.
     * @param detectKind detectKind
     * @param sessionKey sessionKey
     * @return if register, true. not register false.
     */
    public boolean checkRegisterDetectEvent(final HumanDetectKind detectKind, final String sessionKey) {
        
        for (HumanDetectEvent event : mEventArray) {
            if (detectKind == event.getKind() && sessionKey.equals(event.getSessionKey())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * check event empty.
     * @return true: empty, false: not empty.
     */
    public boolean isEmptyEvent() {
        if (mEventArray.size() <= 0) {
            return true;
        }
        return false;
    }
    

    
    //
    // device comm process.
    //
    
    /**
     * Start device search thread. 
     * @param context Context
     * @param listener callback listener.
     * @return result
     */
    public DeviceSearchResult startDeviceSearchThread(final Context context, final HvcDeviceSearchListener listener) {
        if (mDeviceSearchThread == null || !mDeviceSearchThread.isAlive()) {
            mDeviceSearchThread = new HvcDeviceSearchThread(context, listener); 
            mDeviceSearchThread.start();
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
       if (mDetectThread == null || !mDetectThread.isAlive()) {
            mDetectThread = new HvcDetectThread(context, device, useFunc, params, listener);
            mDetectThread.start();
            return DetectionResult.RESULT_SUCCESS;
        } else {
            return DetectionResult.RESULT_ERR_THREAD_ALIVE;
        }
    }

    // 
    // store bluetooth devices.
    // 
    
    /**
     * BluetoothDevices(found by service discovery).
     */
    private static List<BluetoothDevice> sDevices = new ArrayList<BluetoothDevice>();
    
    /**
     * Store BluetoothDevices(found by service discovery).
     * @param devices BluetoothDevices
     */
    public static void storeDevices(final List<BluetoothDevice> devices) {
        synchronized (sDevices) {
            sDevices = devices;
        }
    }
    
    /**
     * Search BluetoothDevice.
     * @param serviceId serviceId
     * @return not null: found BluetoothDevice / null:not found.
     */
    public static BluetoothDevice searchDevices(final String serviceId) {
        
        synchronized (sDevices) {
            if (sDevices != null) {
                for (BluetoothDevice device : sDevices) {
                    if (serviceId.equals(getServiceId(device.getAddress()))) {
                        return device;
                    }
                }
            }
            return null;
        }
    }
    
    /**
     * Get serviceId from bluetoothAddress.
     * @param address bluetoothAddress
     * @return serviceId
     */
    public static String getServiceId(final String address) {
        String serviceId = address.replace(":", "").toLowerCase(Locale.ENGLISH);
        return serviceId;
    }
    
    
    
    // 
    // get data by event registers.
    // 

    /**
     * get use func by event registers.
     * @return useFunc useFunc
     */
    public int getUseFuncByEventRegisters() {
        int useFunc = 0;
        for (HumanDetectEvent event : mEventArray) {
            useFunc |= HvcConvertUtils.convertUseFunc(event.getKind(), event.getOptions());
        }
        return useFunc;
    }

    
}
