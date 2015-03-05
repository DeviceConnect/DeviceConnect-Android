/*
 HvcDetectThread.java
 Copyright (c) 2015 NTT DOCOMO,INC.
 Released under the MIT license
 http://opensource.org/licenses/mit-license.php
 */
package org.deviceconnect.android.deviceplugin.hvc.comm;

import org.deviceconnect.android.deviceplugin.hvc.humandetect.HumanDetectRequestParams;
import org.deviceconnect.android.deviceplugin.hvc.request.HvcDetectRequestParams;

import omron.HVC.HVC;
import omron.HVC.HVCBleCallback;
import omron.HVC.HVC_BLE;
import omron.HVC.HVC_PRM;
import omron.HVC.HVC_RES;
import android.bluetooth.BluetoothDevice;
import android.content.Context;

/**
 * HVC Detect thread.
 * 
 * @author NTT DOCOMO, INC.
 */
public class HvcDetectThread extends Thread {
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
    HVC_PRM mHvcPrm = new HVC_PRM();
    /**
     * HVC response class.
     */
    HVC_RES mHvcRes = new HVC_RES();
    
    /**
     * Constructor.
     * @param context Context
     * @param device bluetooth device
     * @param useFunc use function bit flag
     * @param params parameter
     * @param listener listener
     */
    public HvcDetectThread(final Context context, final BluetoothDevice device, final int useFunc,
            final HumanDetectRequestParams params, final HvcDetectListener listener) {
        super();
        mContext = context;
        mDevice = device;
        mUseFunc = useFunc;
        mHvcPrm = (new HvcDetectRequestParams(params)).getHvcParams();
        mListener = listener;
    }
    
    @Override
    public void run() {
        
        // BLE initialize (GATT)
        mHvcBle.setCallBack(new HVCBleCallback() {
            @Override
            public void onConnected() {
                super.onConnected();
                
                // send parameter command to HVC.(results return callback. : mCallback.onPostSetParam())
                int result = mHvcBle.setParam(mHvcPrm);
                if (result != HVC.HVC_NORMAL) {
                    mListener.onConnectError(result);
                    return;
                }
            }
            
            @Override
            public void onDisconnected() {
                mListener.onDetectFaceDisconnected();
                super.onDisconnected();
            }
            
            @Override
            public void onPostSetParam(final int nRet, final byte outStatus) {
                super.onPostSetParam(nRet, outStatus);
                
                // send detect command to HVC.(results return callback. : mCallback.onPostExecute())
                int result = mHvcBle.execute(mUseFunc, mHvcRes);
                if (result != HVC.HVC_NORMAL) {
                    mListener.onRequestDetectError(result);
                    return;
                }
            }
            
            @Override
            public void onPostExecute(final int nRet, final byte outStatus) {
                if (nRet != HVC.HVC_NORMAL || outStatus != 0) {
                    // Error processing
                    mListener.onDetectError(nRet);
                } else {
                    mListener.onDetectFinished(mHvcRes);
                }
                
                // disconnect
                mHvcBle.disconnect();
            }
        });
        mHvcBle.connect(mContext, mDevice);
        
        
    }
}
