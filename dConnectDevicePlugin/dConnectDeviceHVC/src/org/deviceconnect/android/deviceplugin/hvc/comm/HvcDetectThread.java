/*
 HvcDetectThread.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;

import omron.HVC.HVC;
import omron.HVC.HVCBleCallback;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

/**
 * HVC Detect thread.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDetectThread extends Thread {
    
    /**
     * log tag.
     */
    private static final String TAG = HvcDetectThread.class.getSimpleName();

    /**
     * Constructor.
     */
    private Context mContext;
    /**
     * Bluetooth Device.
     */
    private BluetoothDevice mDevice;
    
    /**
     * Use function bit flag.
     */
    private int mUseFunc;
    /**
     * HVC detect listener.
     */
    private HvcDetectListener mListener;
    
    /**
     * HVC BLE class.
     */
    HVC_BLE mHvcBle = new HVC_BLE();
    /**
     * HVC parameter class.
     */
    HVC_PRM mHvcPrm = null;
    /**
     * HVC response class.
     */
    HVC_RES mHvcRes = new HVC_RES();
    
    /**
     * HVC parameter class lock object.
     */
    private Object mHvcParameterLock = new Object();
    
    /**
     * thread running flag.
     */
    private boolean mIsRunning = false;
    
    /**
     * connecting flag.
     */
    private boolean mIsConnecting = false;
    
    
    /**
     * post set parameter command wait flag.
     */
    private boolean mIsWaitPostSetParameter = false;
    
    /**
     * post detect wait flag.
     */
    private boolean mIsWaitPostDetect = false;

    /**
     * thread sleep time[msec].
     */
    private static final long THREAD_SLEEP_TIME = 500;
    
    
    
    /**
     * Constructor.
     * @param context Context
     * @param device bluetooth device
     * @param listener listener
     */
    public HvcDetectThread(final Context context, final BluetoothDevice device, final HvcDetectListener listener) {
        super();
        mContext = context;
        mDevice = device;
        mListener = listener;
    }

    /**
     * thread halt process.
     */
    public void halt() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "HvcDetectThread - halt()");
        }
        mIsRunning = false;
        mHvcBle.disconnect();
        interrupt();
    }
    
    
    /**
     * connect process.
     */
    public void connectProc() {
        mIsConnecting = true;
    }
    
    /**
     * send request.
     * @param useFunc use function bit flag
     * @param param parameter
     * @return true: success / false: error(call mListener.onSetParamError(result))
     */
    public boolean sendRequest(final int useFunc, final HVC_PRM param) {
        
        // send parameter command to HVC.(results return callback. : mCallback.onPostSetParam())
        mUseFunc = useFunc;
        mHvcPrm = param;
        int result = mHvcBle.setParam(mHvcPrm);
        if (result != HVC.HVC_NORMAL) {
            // error (device not found, disconnected, busy ...)
            mListener.onSetParamError(result);
            return false;
        }
        return true;
    }
    
    /**
     * post parameter.
     * @param useFunc useFunc.
     * @param hvcPrm HVC parameters.
     */
    public void postSetParameter(final int useFunc, final HVC_PRM hvcPrm) {
        synchronized (mHvcParameterLock) {
            mUseFunc = useFunc;
            mHvcPrm = hvcPrm;
            mIsWaitPostSetParameter = true;
        }
    }

    /**
     * post detect.
     */
    public void postDetect() {
        mIsWaitPostDetect = true;
    }
    
    @Override
    public void run() {
        
        // BLE initialize (GATT)
        mHvcBle.setCallBack(new HVCBleCallback() {
            @Override
            public void onConnected() {
                super.onConnected();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "HvcDetectThread - onConnected()");
                }
                mListener.onConnected();
                
            }
            
            @Override
            public void onDisconnected() {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "HvcDetectThread - onDisconnected()");
                }
                mListener.onDisconnected();
                super.onDisconnected();
            }
            
            @Override
            public void onPostSetParam(final int nRet, final byte outStatus) {
                super.onPostSetParam(nRet, outStatus);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "HvcDetectThread - onPostSetParam()");
                }
                mListener.onPostSetParam(mHvcPrm);
            }
            
            @Override
            public void onPostExecute(final int nRet, final byte outStatus) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "HvcDetectThread - onPostExecute() nRet:" + nRet + " outStatus:" + outStatus);
                }
                if (nRet != HVC.HVC_NORMAL || outStatus != 0) {
                    // Error processing
                    mListener.onDetectError(nRet);
                } else {
                    mListener.onDetectFinished(mHvcPrm, mHvcRes);
                }
                
            }
        });
        
        mIsRunning = true;
        while (mIsRunning) {

            int commStatus = mHvcBle.getStatus();
            if (mIsConnecting && commStatus == HVC.HVC_ERROR_NODEVICES) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "mHvcBle.connect()");
                }
                mHvcBle.connect(mContext, mDevice);
                
            }
            
            if (mHvcBle != null && mIsWaitPostSetParameter) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "mHvcBle.setParam()");
                }
                int result = mHvcBle.setParam(mHvcPrm);
                if (result != HVC.HVC_NORMAL) {
                    mListener.onSetParamError(result);
                    return;
                }
                mIsWaitPostSetParameter = false;
            }
            
            if (mHvcBle != null && mIsWaitPostDetect) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "mHvcBle.execute()");
                }
                int result = mHvcBle.execute(mUseFunc, mHvcRes);
                if (result != HVC.HVC_NORMAL) {
                    mListener.onRequestDetectError(result);
                    return;
                }
                mIsWaitPostDetect = false;
            }
            
            
            // sleep.
            try {
                sleep(THREAD_SLEEP_TIME);
            } catch (InterruptedException e) {
                if (BuildConfig.DEBUG) {
                    e.printStackTrace();
                }
            }
        }
        
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "run() finish. disconnected.");
        }
        mHvcBle.disconnect();
    }
    
    /**
     * get comm status.
     * @return comm status.
     */
    public int getHvcCommStatus() {
        int commStatus = mHvcBle.getStatus();
        return commStatus;
    }
}
