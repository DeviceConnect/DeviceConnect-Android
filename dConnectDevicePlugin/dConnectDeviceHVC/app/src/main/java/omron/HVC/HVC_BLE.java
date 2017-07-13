/*
 * Copyright (C) 2014 OMRON Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package omron.HVC;

import java.util.ArrayList;

import org.deviceconnect.android.deviceplugin.hvc.BuildConfig;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

interface BleInterface {
    abstract void connect(Context context, BluetoothDevice device);
    abstract void disconnect();
    abstract int setDeviceName(byte[] value);
    abstract int getDeviceName(byte[] value);
}

/**
 * HVC-C BLE Model<br>
 * [Description]<br>
 * HVC subclass, connects HVC to Bluetooth<br>
 * 
 */
public class HVC_BLE extends HVC implements BleInterface
{
    /**
     * DebugLog.
     */
    static final boolean DEBUG_LOG = BuildConfig.DEBUG;
    
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_BUSY = 3;

    private int mStatus = STATE_DISCONNECTED;
    private byte[] mtxName = null;
    private ArrayList<Byte> mtxValue = null;

    private HVCBleCallback mCallback = null;
    private BluetoothDevice mBtDevice = null;
    private BleDeviceService mService = null;

    private static final String TAG = "HVC_BLE";

    /**
     * HVC_BLE constructor<br>
     * [Description]<br>
     * Set HVC_BLE to new to automatically connect to Bluetooth device specified with btDevice<br>
     * @param mainAct Activity object<br>
     */
    public HVC_BLE()
    {
        super();
    	mStatus = STATE_DISCONNECTED;
        mtxValue = new ArrayList<Byte>();
    }

    /**
     * HVC_BLE finalizer<br>
     * [Description]<br>
     * MUST be called when ending<br>
     * @throws Throwable 
     */
    @Override
    public void finalize() throws Throwable {
        mStatus = STATE_DISCONNECTED;
        if ( mService != null ) {
            mService.close();
        }
        mService = null;
        super.finalize();
    }

    /**
     * HVC_BLE verify status<br>
     * [Description]<br>
     * Verify HVC_BLE device status<br>
     * @return boolean true:function executable, false:function non-executable<br>
     */
    @Override
    public boolean IsBusy() {
        // TODO Auto-generated method stub
        if ( mStatus != STATE_CONNECTED ) {
            return true;
        }
        return false;
    }

    /**
     * Execute HVC functions<br>
     * [Description]<br>
     * Execute each HVC function. Store results in HVC_BLE.Result<br>
     * @param inExec execution flag<br>
     * @param outStatus HVC execution result status<br>
     * @return int execution result error code <br>
     */
    @Override
    public int execute(final int inExec, final HVC_RES res)
    {
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "execute() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "execute() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "execute() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

    	Thread t = new Thread() {
			public void run() {
		        int nRet = HVC_NORMAL;
		        byte[] outStatus = new byte[1];
		        nRet = Execute(30000, inExec, outStatus, res);

                if ( mStatus == STATE_BUSY ) {
                    mStatus = STATE_CONNECTED;
                }

                if ( mCallback != null ) {
		        	mCallback.onPostExecute(nRet, outStatus[0]);
		        }
			}
    	};

    	t.start();
        if (DEBUG_LOG) {
            Log.d(TAG, "execute() : HVC_NORMAL");
        }
    	return HVC_NORMAL;
    }

    @Override
    public int setParam(final HVC_PRM prm)
    {
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "setParam() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "setParam() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "setParam() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;
    	Thread t = new Thread() {
			public void run() {
		        int nRet = HVC_NORMAL;
		        byte[] outStatus = new byte[1];
		        
		        nRet = SetCameraAngle(10000, outStatus, prm);
		        if ( nRet == HVC_NORMAL && outStatus[0] == 0 ) {
		            nRet = SetThreshold(10000, outStatus, prm);
		        }
		        if ( nRet == HVC_NORMAL && outStatus[0] == 0 ) {
		            nRet = SetSizeRange(10000, outStatus, prm);
		        }
		        if ( nRet == HVC_NORMAL && outStatus[0] == 0 ) {
		            nRet = SetFaceDetectionAngle(10000, outStatus, prm);
		        }

                if ( mStatus == STATE_BUSY ) {
                    mStatus = STATE_CONNECTED;
                }
		        if ( mCallback != null ) {
		        	mCallback.onPostSetParam(nRet, outStatus[0]);
		        }
			}
    	};

    	t.start();
        if (DEBUG_LOG) {
            Log.d(TAG, "setParam() : HVC_NORMAL");
        }
    	return HVC_NORMAL;
    }

    public int getParam(final HVC_PRM prm) {
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getParam() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getParam() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getParam() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

    	Thread t = new Thread() {
			public void run() {
		        int nRet = HVC_NORMAL;
		        byte[] outStatus = new byte[1];
		        if ( nRet == HVC_NORMAL ) {
		            nRet = GetCameraAngle(10000, outStatus, prm);
		        }
		        if ( nRet == HVC_NORMAL ) {
		            nRet = GetThreshold(10000, outStatus, prm);
		        }
		        if ( nRet == HVC_NORMAL ) {
		            nRet = GetSizeRange(10000, outStatus, prm);
		        }
		        if ( nRet == HVC_NORMAL ) {
		            nRet = GetFaceDetectionAngle(10000, outStatus, prm);
		        }
		        if ( mCallback != null ) {
		        	mCallback.onPostGetParam(nRet, outStatus[0]);
		        }

				if ( mStatus == STATE_BUSY ) {
			        mStatus = STATE_CONNECTED;
				}
			}
    	};

    	t.start();
        if (DEBUG_LOG) {
            Log.d(TAG, "getParam() : HVC_NORMAL");
        }
    	return HVC_NORMAL;
    }

	@Override
	public int getVersion(final HVC_VER ver) {
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getParam() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getParam() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getParam() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }

        mStatus = STATE_BUSY;

    	Thread t = new Thread() {
			public void run() {
		        int nRet = HVC_NORMAL;
		        byte[] outStatus = new byte[1];
		        if ( nRet == HVC_NORMAL ) {
		            nRet = GetVersion(10000, outStatus, ver);
		        }
		        if ( mCallback != null ) {
		        	mCallback.onPostGetVersion(nRet, outStatus[0]);
		        }

				if ( mStatus == STATE_BUSY ) {
			        mStatus = STATE_CONNECTED;
				}
			}
    	};

    	t.start();
        if (DEBUG_LOG) {
            Log.d(TAG, "getVersion() : HVC_NORMAL");
        }
    	return HVC_NORMAL;
	}

    private final BleCallback gattCallback = new BleCallback() {
	    @Override
	    public void callbackMethod(String action) {
	        //*********************//
	        if (action.equals(BleDeviceService.ACTION_GATT_CONNECTED)) {
                if (DEBUG_LOG) {
                    Log.d(TAG, "UART_CONNECT_MSG");
                }
	            mStatus = STATE_CONNECTING;
	        }
	        
	        //*********************//
	        if (action.equals(BleDeviceService.ACTION_GATT_DISCONNECTED)) {
                if (DEBUG_LOG) {
                    Log.d(TAG, "UART_DISCONNECT_MSG");
                }
	            mService.close();
	            mStatus = STATE_DISCONNECTED;
		        if ( mCallback != null ) {
		        	mCallback.onDisconnected();
		        }
	        }
	        
	        //*********************//
	        if (action.equals(BleDeviceService.ACTION_GATT_SERVICES_DISCOVERED)) {
                if (DEBUG_LOG) {
                    Log.d(TAG, "UART_DISCOVERED_MSG");
                }
	            mStatus = STATE_CONNECTED;
		        if ( mCallback != null ) {
		        	mCallback.onConnected();
		        }
	        }

	        //*********************//
	        if (action.equals(BleDeviceService.DEVICE_DOES_NOT_SUPPORT_UART)){
                if (DEBUG_LOG) {
                    Log.d(TAG, "DEVICE_DOES_NOT_SUPPORT_UART");
                }
	            mService.disconnect();
	            mStatus = STATE_DISCONNECTED;
	        }
	    }

    	@Override
	    public void callbackMethod(String action, byte[] byText) {
	        //*********************//
	        if (action.equals(BleDeviceService.ACTION_DATA_AVAILABLE+BleDeviceService.EXTRA_DATA)) {
	            if ( byText != null ) {
	                String deviceInfo = "DATA_AVAILABLE: " + String.valueOf(byText.length) + " byte";
	                synchronized (mtxValue) {
	                    for ( int i=0; i<byText.length; i++ ) {
	                        mtxValue.add(byText[i]);
	                        //deviceInfo += String.valueOf(byText[i]) + " ";
	                    }
	                }
	                if (DEBUG_LOG) {
	                    Log.d(TAG, deviceInfo);
	                }
	                sleep(1);
	            }
	        }

	        //*********************//
	        if (action.equals(BleDeviceService.ACTION_DATA_AVAILABLE+BleDeviceService.NAME_DATA)) {
	            if ( byText != null && mtxName != null ) {
	                String deviceInfo = "NAME_AVAILABLE: " + String.valueOf(byText.length) + " byte";
	                synchronized (mtxName) {
	                    for ( int i=0; i<byText.length; i++ ) {
	                    	mtxName[i] = byText[i];
	                        //deviceInfo += String.valueOf(mtxName[i]) + " ";
	                    }
	                }
	                if (DEBUG_LOG) {
	                    Log.d(TAG, deviceInfo);
	                }
			        if ( mCallback != null ) {
			        	mCallback.onPostGetDeviceName(mtxName);
			        }
	            }
	        }
	    }
    };

    /**
     * Bt send signal<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inDataSize send signal data size<br>
     * @param inData send signal data<br>
     * @return int send signal complete data number<br>
     */
    @Override
    protected int Send(byte[] inData)
    {
        do {
            if ( mStatus < STATE_CONNECTED ) {
                return 0;
            }
            synchronized(mtxValue){
                int readLength = mtxValue.size();
                if ( readLength <= 0 ) break;
                mtxValue.clear();
            }
        } while ( true );
        mService.writeTXCharacteristic(inData);

    	String deviceInfo = "Send: " + inData.length + " byte";
        if (DEBUG_LOG) {
            Log.d(TAG, deviceInfo);
        }
        return inData.length;
    }

    /**
     * Bt receive signalg<br>
     * [Description]<br>
     * none<br>
     * [Notes]<br>
     * @param inDataSize receive signal data size<br>
     * @param outResult receive signal data<br>
     * @return int receive signal complete data number<br>
     */
    @Override
    protected int Receive(int inTimeOutTime, int inDataSize, byte[] outResult)
    {
        long maxTimeMillis = System.currentTimeMillis() + inTimeOutTime;
        while (System.currentTimeMillis() < maxTimeMillis) {
            if ( mStatus < STATE_CONNECTED ) {
                return 0;
            }
            int readLength = mtxValue.size();
            if ( readLength >= inDataSize ) {
                break;
            } else {
                sleep(1);
            }
        }
        int readLength = 0;
        synchronized(mtxValue){
            readLength = java.lang.Math.min(mtxValue.size(),inDataSize);
            for ( int i=0; i<readLength; i++ ) {
                outResult[i] = mtxValue.get(i);
            }
            for ( int i=0; i<readLength; i++ ) {
                mtxValue.remove(0);
            }
        }
        String deviceInfo = "Receive: " + String.valueOf(readLength) + " byte";
        if (DEBUG_LOG) {
            Log.d(TAG, deviceInfo);
        }
        return readLength;
    }

    protected synchronized void sleep(long msec) {
        //Method to stop execution after set number of msec
        try {
            wait(msec);
        } catch(InterruptedException e){}
    }

	public void setCallBack(HVCBleCallback hvcCallback) {
		// TODO Auto-generated method stub
		mCallback = hvcCallback;
        if (DEBUG_LOG) {
            Log.d(TAG, "Set CallBack");
        }
	}

    /**
     * HVC_BLE connect<br>
     * [Description]<br>
     * Connect with HVC_BLE device<br>
     */
    @Override
    public synchronized void connect(Context context, BluetoothDevice device) {
        mStatus = STATE_DISCONNECTED;
        if ( mService != null ) {

            if (DEBUG_LOG) {
                Log.d(TAG, "DisConnect Device = " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
            }
            mService.close();
        }

        mBtDevice = device;
        if ( mBtDevice == null ) {
            return;
        }

        mService = new BleDeviceService(gattCallback);

        mService.connect(context, mBtDevice);
        if (DEBUG_LOG) {
            Log.d(TAG, "Connect Device = " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
        }
    }

    /**
     * HVC_BLE disconnect<br>
     * [Description]<br>
     * Disconnect HVC_BLE device<br>
     */
    @Override
    public synchronized void disconnect() {
        mStatus = STATE_DISCONNECTED;
        if ( mService != null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "DisConnect Device = " + mBtDevice.getName() + " (" + mBtDevice.getAddress() + ")");
            }
            mService.close();
        }
        mService = null;
        if ( mCallback != null ) {
        	mCallback.onDisconnected();
        }
    }

	@Override
	public int setDeviceName(byte[] value) {
		// TODO Auto-generated method stub
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "setDeviceName() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "setDeviceName() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "setDeviceName() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }

        mService.writeNameCharacteristic(value);
        if (DEBUG_LOG) {
            Log.d(TAG, "getDeviceName() : HVC_NORMAL");
        }
		return HVC_NORMAL;
	}

	@Override
	public int getDeviceName(byte[] value) {
		// TODO Auto-generated method stub
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getDeviceName() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getDeviceName() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getDeviceName() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }

        mtxName = value;
        mService.readNameCharacteristic();
        if (DEBUG_LOG) {
            Log.d(TAG, "getDeviceName() : HVC_NORMAL");
        }
		return HVC_NORMAL;
	}
	
	/**
	 * get status.
	 * @return status(STATE_DISCONNECTED / STATE_CONNECTING / STATE_CONNECTED / STATE_BUSY)
	 */
	public int getStatus() {
	    return mStatus;
	}
	
	/**
	 * get comm status.
	 * @return status.
	 */
    public int getCommStatus() {
        if ( mBtDevice == null ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getStatus() : HVC_ERROR_NODEVICES");
            }
            return HVC_ERROR_NODEVICES;
        }
        if ( mService == null || mService.getmConnectionState() != BleDeviceService.STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getStatus() : HVC_ERROR_DISCONNECTED");
            }
            return HVC_ERROR_DISCONNECTED;
        }
        if ( mStatus > STATE_CONNECTED ) {
            if (DEBUG_LOG) {
                Log.d(TAG, "getStatus() : HVC_ERROR_BUSY");
            }
            return HVC_ERROR_BUSY;
        }
        if (DEBUG_LOG) {
            Log.d(TAG, "getStatus() : HVC_NORMAL - mStatus:" + mStatus);
        }
        return HVC_NORMAL;
    }
}
